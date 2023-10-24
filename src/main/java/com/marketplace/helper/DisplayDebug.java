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

package com.marketplace.helper;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

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
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;

import static com.marketplace.helper.DisplayDebug.DisplayDebugOutcome.NEXT_OUTCOME;

/**
 * Dipslay Debug Node
 */
@Node.Metadata(outcomeProvider = DisplayDebug.DisplayDebugOutcomeProvider.class, configClass = DisplayDebug.Config.class)
public class DisplayDebug extends AbstractDecisionNode {
	private final Logger logger = LoggerFactory.getLogger(DisplayDebug.class.getName());
	private final Config config;
	private static final String BUNDLE = DisplayDebug.class.getName();
	private String loggerPrefix = "[DisplayDebug Node][Marketplace] ";

	/**
	 * Configuration for the node.
	 */
	public interface Config {
		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 100)
		default boolean sharedState() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 200)
		default boolean authID() {
			return false;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 300)
		default boolean headers() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 400)
		default boolean clientIp() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 500)
		default boolean cookies() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 600)
		default boolean hostName() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 700)
		default boolean locales() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
		@Attribute(order = 800)
		default boolean parameters() {
			return true;
		}

		/**
		 * The header name for zero-page login that will contain the identity's
		 * username.
		 */
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

		try {
			ArrayList<Callback> callbacks = new ArrayList<Callback>();
			List<StringAttributeInputCallback> stringCallbacks = context.getCallbacks(StringAttributeInputCallback.class);

			if (context.hasCallbacks()) {

				logger.debug(loggerPrefix + "Done.");
				NodeState ns = context.getStateFor(this);

				//For loop to itterate through StringAttribute callbacks
				for (StringAttributeInputCallback callback : stringCallbacks) {

					//Get key, value pair in stringCallbacks
					String key = callback.getName();
					String value = callback.getValue();
					System.out.println("Key: " + key + "\nValue: " + value);
					if(key.equals("authLevel")){
						int int_value = Integer.parseInt(value);
						ns.putShared(key, int_value);
					}
					//callbacks.add(new StringAttributeInputCallback(key, key + ": ", value.toString(), false));

					ns.putShared(key, value);
					//ns.get(key).put(key, value);
					//JsonValue copyState = context.sharedState.copy();
					//return Action.goTo(NEXT_OUTCOME.name()).build();
				}

				return Action.goTo(NEXT_OUTCOME.name()).build();
			}

			//ArrayList<Callback> callbacks = new ArrayList<Callback>();
			callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, " "));
			if (config.sharedState()) {

				NodeState ns = context.getStateFor(this);
				Set<String> shareStateKeys = ns.keys();
				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "NODE STATE - FTW"));

				for (Iterator<String> i = shareStateKeys.iterator(); i.hasNext();) {
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
				for (Iterator<String> i = headersKey.iterator(); i.hasNext();) {
					String thisKey = i.next();
					List thisHeaderVal = headers.get(thisKey);
					TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisHeaderVal.toString()));
					StringAttributeInputCallback stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", html2text(escapeHTML(thisHeaderVal.toString())), false);
					callbacks.add(txtOutputCallback);
					callbacks.add(stringCallback);

				}
			}

			if (config.clientIp()) {

				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "CLIENT IP"));
				String theClientIP = context.request.clientIp;
				TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "ClientIP" + ": " + escapeHTML(theClientIP));
				StringAttributeInputCallback stringCallback = new StringAttributeInputCallback("ClientIp", "ClientIp: ", escapeHTML(theClientIP.toString()), false);
				callbacks.add(txtOutputCallback);
				callbacks.add(stringCallback);

			}

			if (config.cookies() && context.request != null && context.request.cookies != null) {

				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "COOKIES"));
				Map<String, String> theCookies = context.request.cookies;
				Set<String> cookieKeys = theCookies.keySet();
				for (Iterator<String> i = cookieKeys.iterator(); i.hasNext();) {
					String thisKey = i.next();
					String thisCookieVal = theCookies.get(thisKey);
					TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisCookieVal.toString()));
					StringAttributeInputCallback stringCallback = new StringAttributeInputCallback(thisKey, thisKey+": ", escapeHTML(thisCookieVal.toString()), false);
					callbacks.add(txtOutputCallback);
					callbacks.add(stringCallback);


				}
			}

			if (config.hostName()) {
				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "HOSTNAME"));
				String theHostName = context.request.hostName;
				TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "HostName" + ": " + escapeHTML(theHostName));
				StringAttributeInputCallback stringCallback = new StringAttributeInputCallback("Hostname", "Hostname: ", escapeHTML(theHostName), false);
				callbacks.add(txtOutputCallback);
				callbacks.add(stringCallback);

			}

			if (config.locales()) {

				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "LOCALE"));
				PreferredLocales theLocales = context.request.locales;
				TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Preferred Locale" + ": " + escapeHTML(theLocales.getPreferredLocale().getDisplayName()));
				StringAttributeInputCallback stringCallback = new StringAttributeInputCallback("Preferred Locale", "Preferred Locale: ", escapeHTML(theLocales.getPreferredLocale().getDisplayName()), false);
				callbacks.add(txtOutputCallback);
				callbacks.add(stringCallback);

			}

			if (config.parameters() && context.request != null && context.request.parameters != null) {

				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "PARAMETERS"));
				Map<String, List<String>> theParms = context.request.parameters;
				Set<String> parmKeys = theParms.keySet();
				for (Iterator<String> i = parmKeys.iterator(); i.hasNext();) {
					String thisKey = i.next();
					List<String> thisParmVal = theParms.get(thisKey);
					TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisParmVal.toString()));

					StringAttributeInputCallback stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", escapeHTML(thisParmVal.toString()), false);

					callbacks.add(txtOutputCallback);

					callbacks.add(stringCallback);

				}
			}

			if (config.serverUrl()) {

				callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, "SERVER URL"));
				String theServerURL = context.request.serverUrl;
				TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Server URL" + ": " + escapeHTML(theServerURL));

				StringAttributeInputCallback stringCallback = new StringAttributeInputCallback("Server Url", "Server Url" + ": ", escapeHTML(theServerURL), false);

				callbacks.add(txtOutputCallback);
				callbacks.add(stringCallback);

			}

			String scriptedForFormating = "" + "for (const val of document.querySelectorAll('div')) {\n" + "  if(val.textContent === \"NODE STATE - FTW\" ||\n" + "    val.textContent === \"AUTHID\" ||\n" + "    val.textContent === \"HEADERS\" ||\n" + "    val.textContent === \"CLIENT IP\" ||\n" + "    val.textContent === \"COOKIES\" ||\n" + "    val.textContent === \"HOSTNAME\" ||\n" + "    val.textContent === \"LOCALE\" ||\n" + "    val.textContent === \"PARAMETERS\" ||\n" + "    val.textContent === \"SERVER URL\")\n" + "    val.outerHTML = \"<h3>\" + val.outerHTML + \"</h3>\";\n" + "}";
			ScriptTextOutputCallback scriptAndSelfSubmitCallback = new ScriptTextOutputCallback(scriptedForFormating);
			//callbacks.add(scriptAndSelfSubmitCallback);

			return Action.send(callbacks).build();
		} catch (Exception ex) {
			logger.error(loggerPrefix + "Exception occurred: " + ex.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			logger.error(loggerPrefix + sw.toString());
			context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + ex.toString());
			return Action.goTo(DisplayDebugOutcome.ERROR_OUTCOME.name()).build();
		}
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

		/**
		 * Error occured. Need to check sharedstate for issue
		 */
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
