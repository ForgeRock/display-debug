/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */
/*
 * This code is to be used exclusively in connection with ForgeRock’s software or services.
 * ForgeRock only offers ForgeRock software or services to legal entities who have entered
 * into a binding license agreement with ForgeRock.
 */


package com.marketplace.helper;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import javax.inject.Inject;
import javax.print.attribute.IntegerSyntax;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.forgerock.guava.common.collect.ListMultimap;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.NodeState;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.authentication.callbacks.StringAttributeInputCallback;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.openam.headers.SetHeadersFilter;
import org.forgerock.util.i18n.PreferredLocales;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;

import static com.marketplace.helper.DisplayDebug.DisplayDebugOutcome.NEXT_OUTCOME;

/**
 * Display Debug Node
 */
@Node.Metadata(outcomeProvider = DisplayDebug.DisplayDebugOutcomeProvider.class, configClass = DisplayDebug.Config.class)
public class DisplayDebug extends AbstractDecisionNode {
	private final Logger logger = LoggerFactory.getLogger(DisplayDebug.class.getName());
	private final Config config;
	private static final String BUNDLE = DisplayDebug.class.getName();
	private String loggerPrefix = "[DisplayDebug Node][Marketplace] ";
	@Node.Metadata(outcomeProvider = DisplayDebug.OutcomeProvider.class,
			configClass 		= DisplayDebug.Config.class,
			tags 				= {"marketplace", "trustnetwork"}
	)

	/**
	 * Configuration for the node.
	 */
	public interface Config {
		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 50)
		default boolean display() {
			return true;
		}
		@Attribute(order = 75)
		default boolean textBoxes() {
			return true;
		}
		@Attribute(order = 77)
		default Integer numTextboxes() {
			return 1;
		}

		@Attribute(order = 80)
		default boolean pretty() {
			return true;
		}

		@Attribute(order = 100)
		default boolean sharedState() {
			return true;
		}

		@Attribute(order = 200)
		default boolean authID() {
			return true;
		}

		@Attribute(order = 300)
		default boolean headers() {
			return true;
		}

		@Attribute(order = 400)
		default boolean clientIp() {
			return true;
		}

		@Attribute(order = 500)
		default boolean cookies() {
			return true;
		}

		@Attribute(order = 600)
		default boolean hostName() {
			return true;
		}

		@Attribute(order = 700)
		default boolean locales() {
			return true;
		}

		@Attribute(order = 800)
		default boolean parameters() {
			return true;
		}

		@Attribute(order = 900)
		default boolean serverUrl() {
			return true;
		}

	}

	/**
	 * Create the node using Guice injection. Just-in-time bindings can be used to
	 * obtain instances of other classes from the plugin.
	 *
	 * @param config The service config.
	 * @param realm  The realm the node is in.
	 * @throws NodeProcessException If the configuration was not valid.
	 */
	@Inject
	public DisplayDebug(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
		this.config = config;
	}

	@Override
	public Action process(TreeContext context) throws NodeProcessException {

		if(config.display()) {
				try {
					ArrayList<Callback> callbacks = new ArrayList<Callback>();
					List<StringAttributeInputCallback> stringCallbacks = context.getCallbacks(StringAttributeInputCallback.class);
					if (context.hasCallbacks()) {
						logger.debug(loggerPrefix + "Done.");
						NodeState ns = context.getStateFor(this);

						Set<String> shareStateKeys = ns.keys();
						int i = 0;
						int size = stringCallbacks.size();
						int pair = 2;

						//For loop to iterate through StringAttribute callbacks
						while (i < size) {

							StringAttributeInputCallback currentCallback = stringCallbacks.get(i);

							//Get key, value pair in stringCallbacks
							String key = currentCallback.getName();
							String value = currentCallback.getValue();

							if (key.startsWith("text-boxes") && pair == 2) {
								pair--;
								StringAttributeInputCallback valueCallback = stringCallbacks.get(i + 1);
								String textboxKey = valueCallback.getName();
								if (!textboxKey.startsWith("text-boxes")) {
									i = i + 1;
									continue;
								}
								String val = valueCallback.getValue();
								ns.putShared(value, val);
								i = i + 1;
								continue;
							}
							pair--;
							if (pair == 0) {
								pair = 2;
							}

							//Test what value is coming in
							//To put values in shared state
							for (String thisKey : shareStateKeys) {
								if (key.equals(thisKey)) {
									logger.debug(loggerPrefix + "Key: " + key + " Value: " + value + " being inserted into shared state...");
									JsonValue jsonValue = ns.get(thisKey);

									if (jsonValue.isBoolean()) {
										ns.putShared(key, Boolean.valueOf(String.valueOf(value)));
										continue;
									} else if (jsonValue.isString()) {
										ns.putShared(key, value.toString());
										continue;
									} else if (jsonValue.toString().startsWith("{")) {
										ns.putShared(key, value);
										continue;
									} else if (key.equals("authLevel")) {
										ns.putShared(key, Integer.valueOf(value));
										continue;
									}
								}
							}
							i = i + 1;
						}

						return Action.goTo(NEXT_OUTCOME.name()).build();
					}

					if (config.sharedState()) {
						NodeState ns = context.getStateFor(this);
						Set<String> shareStateKeys = ns.keys();
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "NODE STATE"));
						if(config.textBoxes()) {
							Integer numtextboxes = config.numTextboxes();
							for(int i = 0; i < numtextboxes; i++) {
								StringAttributeInputCallback userCallback_key;
								StringAttributeInputCallback userCallback_value;

								TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Key");
								userCallback_key = new StringAttributeInputCallback("text-boxes", "Key" + ": ", " ", false);

								callbacks.add(txtOutputCallback);
								callbacks.add(userCallback_key);

								TextOutputCallback txtOutputCallback2 = new TextOutputCallback(TextOutputCallback.INFORMATION, "Value");
								userCallback_value = new StringAttributeInputCallback("text-boxes", "Value" + ": ", " ", false);

								callbacks.add(txtOutputCallback2);
								callbacks.add(userCallback_value);
							}
						}

						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "SHARED STATE"));
						for (Iterator<String> i = shareStateKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							JsonValue thisVal = ns.get(thisKey);

							TextOutputCallback txtOutputCallback; // used for label
							StringAttributeInputCallback stringCallback;

							if (thisVal.isString()) {
								txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisVal.asString()));
								stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", thisVal.asString(), false);
							} else {
								txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + thisVal);//inputCallback = new NameCallback(thisVal.toString(), thisVal.toString());
								stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", thisVal.toString(), false);
							}
							callbacks.add(txtOutputCallback);
							callbacks.add(stringCallback);
						}
					}

					if (config.authID()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "AUTHID"));
						String theAuthID = context.request.authId;

						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "AuthID" + ": " + escapeHTML(theAuthID));
						callbacks.add(txtOutputCallback);
					}

					if (config.headers()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "HEADERS"));
						ListMultimap<String, String> headers = context.request.headers;

						Set<String> headersKey = headers.keySet();
						for (Iterator<String> i = headersKey.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							List thisHeaderVal = headers.get(thisKey);
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisHeaderVal.toString()));
							callbacks.add(txtOutputCallback);
						}
					}

					if (config.clientIp()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "CLIENT IP"));
						String theClientIP = context.request.clientIp;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "ClientIP" + ": " + escapeHTML(theClientIP));
						callbacks.add(txtOutputCallback);
					}

					if (config.cookies() && context.request != null && context.request.cookies != null) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "COOKIES"));
						Map<String, String> theCookies = context.request.cookies;
						Set<String> cookieKeys = theCookies.keySet();
						for (Iterator<String> i = cookieKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							String thisCookieVal = theCookies.get(thisKey);
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisCookieVal.toString()));
							callbacks.add(txtOutputCallback);
						}
					}

					if (config.hostName()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "HOSTNAME"));
						String theHostName = context.request.hostName;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "HostName" + ": " + escapeHTML(theHostName));
						callbacks.add(txtOutputCallback);
					}

					if (config.locales()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "LOCALE"));
						PreferredLocales theLocales = context.request.locales;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Preferred Locale" + ": " + escapeHTML(theLocales.getPreferredLocale().getDisplayName()));
						callbacks.add(txtOutputCallback);
					}

					if (config.parameters() && context.request != null && context.request.parameters != null) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "PARAMETERS"));
						Map<String, List<String>> theParms = context.request.parameters;
						Set<String> parmKeys = theParms.keySet();
						for (Iterator<String> i = parmKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							List<String> thisParmVal = theParms.get(thisKey);
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisParmVal.toString()));

							callbacks.add(txtOutputCallback);
						}
					}

					if (config.serverUrl()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "SERVER URL"));
						String theServerURL = context.request.serverUrl;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Server URL" + ": " + escapeHTML(theServerURL));

						callbacks.add(txtOutputCallback);
					}


					if(config.pretty()) {
						//scripts for UI styling
						String node_state_tag = "" + "for (const val of document.querySelectorAll('div')) {\n" + "  if(val.textContent === \"NODE STATE\")\n" + " " +
								"   val.outerHTML = \"<h3 style='border-bottom: 5px solid black; padding-top: 5px'>\"  + val.outerHTML + \"</h3>\";\n" + "}";

						ScriptTextOutputCallback node_state_callback = new ScriptTextOutputCallback(node_state_tag);
						callbacks.add(node_state_callback);

						String h3_tags = "" + "for (const val of document.querySelectorAll('div')) {\n" + "  if(val.textContent === \"AUTHID\" ||\n" + "    val.textContent === \"SHARED STATE\" ||\n" + "    val.textContent === \"HEADERS\" ||\n" + "    val.textContent === \"CLIENT IP\" ||\n" + "    val.textContent === \"COOKIES\" ||\n" + "    val.textContent === \"HOSTNAME\" ||\n" + "    val.textContent === \"LOCALE\" ||\n"
								+ "    val.textContent === \"PARAMETERS\" ||\n" +  "    val.textContent === \"PARAMETERS\" ||\n" + "    val.textContent === \"SERVER URL\")\n" + " " +
								"   val.outerHTML = \"<h3 style='border-top: 5px solid black; padding-top: 5px'>\"  + val.outerHTML + \"</h3>\";\n" + "}";

						ScriptTextOutputCallback scriptAndSelfSubmitCallback = new ScriptTextOutputCallback(h3_tags);
						callbacks.add(scriptAndSelfSubmitCallback);

						String key_val_h4 = "" + "for (const val of document.querySelectorAll('div')) {\n" + "  if(val.textContent === \"Key\" ||\n" + "    val.textContent === \"Value\")\n" + " " +
								"   val.outerHTML = \"<h4>\"  + val.outerHTML + \"</h4>\";\n" + "}";
						ScriptTextOutputCallback tester = new ScriptTextOutputCallback(key_val_h4);
						callbacks.add(tester);



					}

					return Action.send(callbacks).build();
				} catch (Exception ex) {
					String stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
					logger.error(loggerPrefix + "Exception occurred: " + stackTrace);
					context.getStateFor(this).putShared(loggerPrefix + "Exception", ex.getMessage());
					context.getStateFor(this).putShared(loggerPrefix + "StackTrace", stackTrace);
					return Action.goTo(DisplayDebugOutcome.ERROR_OUTCOME.name()).build();
				}
			}
			return Action.goTo(NEXT_OUTCOME.name()).build();
	}




	private static String escapeHTML(String s) {
		StringBuilder out = new StringBuilder(Math.max(16, s.length()));
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
				out.append("&#");
				out.append((int) c);
				out.append(';');
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	public static final String html2text(String html) {
		EditorKit kit = new HTMLEditorKit();
		Document doc = kit.createDefaultDocument();
		doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		try {
			Reader reader = new StringReader(html);
			kit.read(reader, doc, 0);
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {

			return "";
		}
	}

	// Possible outcomes
	public enum DisplayDebugOutcome {

		NEXT_OUTCOME,
		ERROR_OUTCOME
	}

	public static class DisplayDebugOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
		@Override
		public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
			ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, DisplayDebugOutcomeProvider.class.getClassLoader());
			return ImmutableList.of(new Outcome(NEXT_OUTCOME.name(), bundle.getString("nextOutcome")), new Outcome(DisplayDebugOutcome.ERROR_OUTCOME.name(), bundle.getString("errorOutcome")));
		}
	}

}
