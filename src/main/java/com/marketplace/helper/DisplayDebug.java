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
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import com.google.common.html.HtmlEscapers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import groovy.lang.Script;
import it.unimi.dsi.fastutil.Hash;
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
import org.forgerock.util.Pair;
import org.forgerock.util.i18n.PreferredLocales;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.w3c.dom.html.HTMLDocument;

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
		ArrayList<Callback> callbacks = new ArrayList<Callback>();

		if(config.display()) {
				try {
					callbacks = new ArrayList<Callback>();
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
								//Gets next callback to make current and next callback a pair
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

					String h3 = "";
					String h3_close="";


					if (config.pretty()) {
						 h3 = "h3";
						 h3_close="/h3";
					}

					if (config.sharedState()) {
						NodeState ns = context.getStateFor(this);
						Set<String> shareStateKeys = ns.keys();
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3+ "NODE STATE" + h3_close));
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

						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "SHARED STATE" + h3_close));
						for (Iterator<String> i = shareStateKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							JsonValue thisVal = ns.get(thisKey);

							TextOutputCallback txtOutputCallback; // used for label
							StringAttributeInputCallback stringCallback;

							if (thisVal.isString()) {
								txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + escapeHTML(thisVal.asString()));
								stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", thisVal.asString(), false);
							} else {
								txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + ": " + thisVal);
								stringCallback = new StringAttributeInputCallback(thisKey, thisKey + ": ", thisVal.toString(), false);
							}
							callbacks.add(txtOutputCallback);
							callbacks.add(stringCallback);
						}
					}


					if (config.authID()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "AUTHID" +h3_close));
						String theAuthID = context.request.authId;

						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION,  "AuthID" + " " + escapeHTML(theAuthID));
						callbacks.add(txtOutputCallback);
					}

					Boolean on_prem_flag = false;
					Boolean cloud_flag = false;

					List<String> headerKeys = new ArrayList<>();
					if (config.headers()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "HEADERS" + h3_close));
						ListMultimap<String, String> headers = context.request.headers;
						if(headers.containsValue("XMLHttpRequest")){
							System.out.println("In on-prem...");
							on_prem_flag = true;
						}
						else if(headers.containsValue("forgerock-sdk")){
							System.out.println("In cloud for some reason...");
							cloud_flag = true;
						}

						Set<String> headersKey = headers.keySet();
						for (Iterator<String> i = headersKey.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							headerKeys.add("\""+thisKey+"\"");
							List  thisHeaderVal = headers.get(thisKey);
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + " " + escapeHTML(thisHeaderVal.toString()));
							callbacks.add(txtOutputCallback);
						}
					}

					if (config.clientIp()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "CLIENT IP" + h3_close));
						String theClientIP = context.request.clientIp;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "ClientIp" + " " + escapeHTML(theClientIP));
						callbacks.add(txtOutputCallback);
					}


					List <String> cookies = new ArrayList<>();
					if (config.cookies() && context.request != null && context.request.cookies != null) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "COOKIES" +h3_close));
						Map<String, String> theCookies = context.request.cookies;
						Set<String> cookieKeys = theCookies.keySet();
						for (Iterator<String> i = cookieKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							String thisCookieVal = theCookies.get(thisKey);
							cookies.add("\""+thisKey+"\"");
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + " " + escapeHTML(thisCookieVal.toString()));
							callbacks.add(txtOutputCallback);
						}
					}

					if (config.hostName()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "HOSTNAME" + h3_close));
						String theHostName = context.request.hostName;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "HostName" + " " + escapeHTML(theHostName));
						callbacks.add(txtOutputCallback);
					}

					if (config.locales()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "LOCALE" +h3_close));
						PreferredLocales theLocales = context.request.locales;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Preferred Locale" + " " + escapeHTML(theLocales.getPreferredLocale().getDisplayName()));
						callbacks.add(txtOutputCallback);
					}

					List<String> paramKeys = new ArrayList<>();
					if (config.parameters() && context.request != null && context.request.parameters != null) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION, h3 + "PARAMETERS" + h3_close));
						Map<String, List<String>> theParms = context.request.parameters;
						Set<String> parmKeys = theParms.keySet();

						for (Iterator<String> i = parmKeys.iterator(); i.hasNext(); ) {
							String thisKey = i.next();
							List<String> thisParamVal = theParms.get(thisKey);
							paramKeys.add("\""+thisKey+"\"");
							TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, thisKey + " " + escapeHTML(thisParamVal.toString()));
							callbacks.add(txtOutputCallback);
						}
					}

					if (config.serverUrl()) {
						callbacks.add(new TextOutputCallback(TextOutputCallback.INFORMATION,  h3 + "SERVER URL" + h3_close));
						String theServerURL = context.request.serverUrl;
						TextOutputCallback txtOutputCallback = new TextOutputCallback(TextOutputCallback.INFORMATION, "Server URL " + escapeHTML(theServerURL));

						callbacks.add(txtOutputCallback);
					}


					if(config.pretty() && on_prem_flag){
						Integer num = 0;
						displayhtml_OnPrem(callbacks, paramKeys, headerKeys, cookies, num);

					}
					else if(config.pretty() && cloud_flag){
						displayhtml_Cloud(callbacks, paramKeys, headerKeys, cookies);
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

	public void displayhtml_OnPrem(ArrayList<Callback> callbacks, List<String> paramKeys, List<String> headerKeys,List<String> cookies, Integer num){

		String javascript = "\n" +
				"\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        if (val.textContent === \"h3NODE STATE/h3\" ||\n" +
				"            val.textContent === \"h3AUTHID/h3\" ||\n" +
				"            val.textContent === \"h3HEADERS/h3\" ||\n" +
				"            val.textContent === \"h3CLIENT IP/h3\" ||\n" +
				"            val.textContent === \"h3COOKIES/h3\" ||\n" +
				"            val.textContent === \"h3HOSTNAME/h3\" ||\n" +
				"            val.textContent === \"h3LOCALE/h3\" ||\n" +
				"            val.textContent === \"h3PARAMETERS/h3\" ||\n" +
				"            val.textContent === \"h3SERVER URL/h3\" ||\n" +
				"            val.textContent === \"h3SHARED STATE/h3\") {\n" +
				"\n" +
				"            val.outerHTML = \"<h3 style='border-bottom: 2px solid black; padding-top: 5px'>\" + val.outerHTML.replace(\"h3\", \"\").replace(\"/h3\", \"\") + \"</h3>\"\n" +
				"        }\n" +
				"        if (val.textContent === \"Key\" || val.textContent === \"Value\") {\n" +
				"            val.innerHTML = \"<h4>\" + val.outerHTML + \"</h4>\"\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    // For Parameters\n" +
				"    const keys= " + paramKeys + "\n" +

				"    let table = '';\n" +
				"    let first = true;\n" +
				"    let very_end = false;\n" +
				"    let trimmed;\n" +
				"    //Loops through divs\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        //iterates through keys in list to find place I want to make table\n" +
				"        for (key in keys) {\n" +
				"\n" +
				"            if (val.textContent.startsWith(keys[key])) {\n" +
				"                //First time coming into loop so this is the beginning\n" +
				"                if (first === true) {\n" +
				"                    table += \"<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                    first = false;\n" +
				"                }\n" +
				"                //This is the last key\n" +
				"                else if (val.textContent.startsWith(keys[keys.length - 1])) {\n" +
				"                    const string = val.textContent;\n" +
				"                    trimmed = '';\n" +
				"                    for (letter in string) {\n" +
				"                        if (string[letter] === \"[\") {\n" +
				"                            break;\n" +
				"                        } else {\n" +
				"                            trimmed += string[letter]\n" +
				"                        }\n" +
				"\n" +
				"                    }\n" +
				"                    //Creates final row and closes table tag\n" +
				"                    table += \"<tr><td><code>\" + trimmed + \"</code></td><td>\" + val.textContent.replace(keys[key], \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                } else {\n" +
				"                    //If not first or last it creates the table row and removes that div\n" +
				"                    //If I don't remove the div it still has the original value with the old styling\n" +
				"                    const string = val.textContent;\n" +
				"                    trimmed = '';\n" +
				"                    for (letter in string) {\n" +
				"                        if (string[letter] === \"[\") {\n" +
				"                            break;\n" +
				"                        } else {\n" +
				"                            trimmed += string[letter]\n" +
				"                        }\n" +
				"                    }\n" +
				"                    table += \"<tr><td><code>\" + trimmed + \"</code></td><td>\" + val.textContent.replace(keys[key], \"\") + \"</td></tr>\"\n" +
				"                    //<table class=\"table table-hover\"><thead><tr><th class=\"selection-cell-header\" data-row-selection=\"true\"><div class=\"checkbox\"><input class=\"react-bs-select-all\" id=\"checkboxHeader\" name=\"checkboxHeader\" type=\"checkbox\"><label for=\"checkboxHeader\"></label></div></th><th tabindex=\"0\" aria-label=\"Username sortable\" class=\"sortable\">Username<span class=\"order\"><span class=\"dropdown\"><span class=\"caret\"></span></span><span class=\"dropup\"><span class=\"caret\"></span></span></span></th><th tabindex=\"0\">Full name</th><th tabindex=\"0\">Email address</th><th tabindex=\"0\">Status</th></tr></thead><tbody><tr><td class=\"selection-cell\"><div class=\"checkbox\"><input id=\"checkbox0\" name=\"checkbox0\" type=\"checkbox\"><label for=\"checkbox0\"></label></div></td><td title=\"demo\"><span class=\"am-table-icon-cell\"><span class=\"fa-stack fa-lg am-table-icon-cell-stack\"><i class=\"fa fa-circle fa-stack-2x text-primary\"></i><i class=\"fa fa-address-card fa-stack-1x fa-inverse\"></i></span> <span><span>demo</span></span></span></td><td title=\"demo\"><span>demo</span></td><td><span>demo@example.com</span></td><td><span class=\"text-success\"><i class=\"fa fa-check-circle\"></i> Active</span></td></tr><tr><td class=\"selection-cell\"><div class=\"checkbox\"><input id=\"checkbox1\" name=\"checkbox1\" type=\"checkbox\"><label for=\"checkbox1\"></label></div></td><td title=\"test_iproov\"><span class=\"am-table-icon-cell\"><span class=\"fa-stack fa-lg am-table-icon-cell-stack\"><i class=\"fa fa-circle fa-stack-2x text-primary\"></i><i class=\"fa fa-address-card fa-stack-1x fa-inverse\"></i></span> <span><span>test_iproov</span></span></span></td><td title=\"test_iproov\"><span>test_iproov</span></td><td><span>testiproov@mailinator.com</span></td><td><span class=\"text-success\"><i class=\"fa fa-check-circle\"></i> Active</span></td></tr></tbody></table></div>\n" +
				"                    document.getElementById(val.id).remove()\n" +
				"                }\n" +
				"                if (very_end) {\n" +
				"                    val.innerHTML = table;\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    function singleItemTable(string) {\n" +
				"        const keys = [\n" +
				"            \"Server URL\",\n" +
				"            \"HostName\",\n" +
				"            \"Preferred Locale\",\n" +
				"            \"ClientIp\",\n" +
				"            \"AuthID\"\n" +
				"        ];\n" +
				"        let table = '';\n" +
				"        let first = true;\n" +
				"        let very_end = false;\n" +
				"        let key = 0;\n" +
				"        for (const val of document.querySelectorAll('div')) {\n" +
				"            if (val.textContent.startsWith(string)) {\n" +
				"                table += \"<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                if (string === \"HostName\") {\n" +
				"                    table += \"<tr><td><code>\" + \"HostName\" + \"</code></td><td>\" + val.textContent.replace(\"HostName\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"Server URL\") {\n" +
				"                    table += \"<tr><td><code>\" + \"Server URL\" + \"</code></td><td>\" + val.textContent.replace(\"Server URL\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"Preferred Locale\") {\n" +
				"                    table += \"<tr><td><code>\" + \"Preferred Locale\" + \"</code></td><td>\" + val.textContent.replace(\"Preferred Locale\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"ClientIp\") {\n" +
				"                    table += \"<tr><td><code>\" + \"ClientIp\" + \"</code></td><td>\" + val.textContent.replace(\"ClientIp\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"AuthID\") {\n" +
				"                    table += \"<tr><td><code>\" + \"AuthID\" + \"</code></td><td>\" + val.textContent.replace(\"AuthID\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                }\n" +
				"            }\n" +
				"\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    singleItemTable(\"HostName\")\n" +
				"    singleItemTable(\"Server URL\")\n" +
				"    singleItemTable(\"ClientIp\")\n" +
				"    singleItemTable(\"Preferred Locale\")\n" +
				"    singleItemTable(\"AuthID\")\n" +
				"    singleItemTable(\"amlbcookie\")\n" ;
		ScriptTextOutputCallback script = new ScriptTextOutputCallback(javascript);
		callbacks.add(script);

		String headers_table = "\n" +
				"    const headers= " + headerKeys +";\n"+
				"\n" +
				"    let headers_table = '';\n" +
				"    let first_headers = true;\n" +
				"    let very_end_headers = false;\n" +
				"    let trimmed_headers;\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        if (very_end_headers) {\n" +
				"            break;\n" +
				"        }\n" +
				"\n" +
				"        for (key in headers) {\n" +
				"            if (val.textContent.startsWith(headers[key]) && first_headers === true) {\n" +
				"\n" +
				"                if (first_headers === true) {\n" +
				"                    headers_table += \"<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                    first_headers = false;\n" +
				"                }\n" +
				"                //This is the last key\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"            } else if (val.textContent.startsWith(headers[key])) {\n" +
				"\n" +
				"                //If not first or last it creates the table row and removes that div\n" +
				"                //If I don't remove the div it still has the original value with the old styling\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"                }\n" +
				"\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"\n" +
				"            } else if (val.textContent.startsWith(headers[headers.length - 1])) {\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr></table></div>\"\n" +
				"                very_end_headers = true;\n" +
				"                break\n" +
				"\n" +
				"            }\n" +
				"        }\n" +
				"        if (very_end_headers) {\n" +
				"            val.innerHTML = headers_table;\n" +
				"            break\n" +
				"        }\n" +
				"    }\n";

		ScriptTextOutputCallback newer = new ScriptTextOutputCallback(headers_table);
		callbacks.add(newer);

		String single = "   function singleItemTable(string) {\n" +
				"\n" +
				"       let table = '';\n" +
				"\n" +
				"       let very_end = false;\n" +
				"\n" +
				"       for (const val of document.querySelectorAll('div')) {\n" +
				"           if (val.textContent.startsWith(string)) {\n" +
				"               table += \"<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>\";\n" +
				"               if (string === \"HostName\") {\n" +
				"                   table += \"<tr><td><code>\" + \"HostName\" + \"</code></td><td>\" + val.textContent.replace(\"HostName\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"Server URL\") {\n" +
				"                   table += \"<tr><td><code>\" + \"Server URL\" + \"</code></td><td>\" + val.textContent.replace(\"Server URL\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"Preferred Locale\") {\n" +
				"                   table += \"<tr><td><code>\" + \"Preferred Locale\" + \"</code></td><td>\" + val.textContent.replace(\"Preferred Locale\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"ClientIP\") {\n" +
				"                   table += \"<tr><td><code>\" + \"ClientIP\" + \"</code></td><td>\" + val.textContent.replace(\"ClientIP\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"AuthID\") {\n" +
				"                   table += \"<tr><td><code>\" + \"AuthID\" + \"</code></td><td>\" + val.textContent.replace(\"AuthID\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               }else if (string === \"amlbcookie\") {\n" +
				"                   table += \"<tr><td><code>\" + \"amlbcookie\" + \"</code></td><td>\" + val.textContent.replace(\"amlbcookie\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               }\n" +
				"           }\n" +
				"\n" +
				"       }\n" +
				"   }\n" +
				"\n" +
				"   singleItemTable(\"HostName\")\n" +
				"   singleItemTable(\"Server URL\")\n" +
				"   singleItemTable(\"ClientIP\")\n" +
				"   singleItemTable(\"Preferred Locale\")\n" +
				"   singleItemTable(\"AuthID\")\n" +
				"   singleItemTable(\"amlbcookie\")\n";
	ScriptTextOutputCallback single_tables = new ScriptTextOutputCallback(single);
		callbacks.add(single_tables);


	String cookies_table = "  const cookies = cookies\n" +
			"\n" +
			"   let cookies_table = '';\n" +
			"   let first_cookie = true;\n" +
			"   let very_end_cookie = false;\n" +
			"   let trimmed_cookie;\n" +
			"   //Loops through divs\n" +
			"   for (const val of document.querySelectorAll('div')) {\n" +
			"       if (very_end_cookie) {\n" +
			"           break;\n" +
			"       }\n" +
			"\n" +
			"//iterates through keys in list to find place I want to make table\n" +
			"       for (key in cookies) {\n" +
			"           if (val.textContent.startsWith(cookies[key]) && first_cookie === true) {\n" +
			"\n" +
			"//First time coming into loop so this is the beginning\n" +
			"               if (first_cookie === true) {\n" +
			"                   cookies_table += \"<div class ='react-bootstrap-table'><table class='table table-bordered table-striped table-detail'>\";\n" +
			"                   first_cookie = false;\n" +
			"               }\n" +
			"               //This is the last key\n" +
			"               const string = val.textContent;\n" +
			"               trimmed_cookie = '';\n" +
			"               for (letter in string) {\n" +
			"                   if (string[letter] === \"[\") {\n" +
			"                       break;\n" +
			"                   } else {\n" +
			"                       trimmed_cookie += string[letter]\n" +
			"                   }\n" +
			"\n" +
			"               }\n" +
			"               //Creates final row and closes table tag\n" +
			"\n" +
			"               cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr>\"\n" +
			"               val.innerHTML = \"\"\n" +
			"\n" +
			"               if(cookies.length === 1) {\n" +
			"                   cookies_table += \"</table></div>\"\n" +
			"                   very_end_cookie = true\n" +
			"                   break\n" +
			"               }\n" +
			"           } else if (val.textContent.startsWith(cookies[key])) {\n" +
			"\n" +
			"               //If not first or last it creates the table row and removes that div\n" +
			"               //If I don't remove the div it still has the original value with the old styling\n" +
			"               const string = val.textContent;\n" +
			"               trimmed_cookie = '';\n" +
			"               for (letter in string) {\n" +
			"                   if (string[letter] === \"[\") {\n" +
			"                       break;\n" +
			"                   } else {\n" +
			"                       trimmed_cookie += string[letter]\n" +
			"                   }\n" +
			"               }\n" +
			"\n" +
			"               cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr>\"\n" +
			"               val.innerHTML = \"\"\n" +
			"            console.log(cookies[key])\n" +
			"           }\n" +
			"\n" +
			"           else if (val.textContent.startsWith(cookies[cookies.length - 1])) {\n" +
			"               const string = val.textContent;\n" +
			"               trimmed_cookie = '';\n" +
			"               for (letter in string) {\n" +
			"                   if (string[letter] === \"[\") {\n" +
			"                       break;\n" +
			"                   } else {\n" +
			"                       trimmed_cookie += string[letter]\n" +
			"                   }\n" +
			"\n" +
			"               }\n" +
			"               //Creates final row and closes table tag\n" +
			"               cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr></table></div>\"\n" +
			"               very_end_cookie = true;\n" +
			"               val.innerHTML = \"\"\n" +
			"               break\n" +
			"\n" +
			"           }\n" +
			"       }\n" +
			"       if (very_end_cookie) {\n" +
			"           val.innerHTML = cookies_table;\n" +
			"           break\n" +
			"       }\n" +
			"   }\n" +
			"\n";

		ScriptTextOutputCallback script_cookies = new ScriptTextOutputCallback(cookies_table);
		callbacks.add(script_cookies);
	}

	public void displayhtml_Cloud(ArrayList<Callback> callbacks, List<String> paramKeys, List<String> headerKeys,List<String> cookies){

		String javascript = "\n" +
				"\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        if (val.textContent === \"h3NODE STATE/h3\" ||\n" +
				"            val.textContent === \"h3AUTHID/h3\" ||\n" +
				"            val.textContent === \"h3HEADERS/h3\" ||\n" +
				"            val.textContent === \"h3CLIENT IP/h3\" ||\n" +
				"            val.textContent === \"h3COOKIES/h3\" ||\n" +
				"            val.textContent === \"h3HOSTNAME/h3\" ||\n" +
				"            val.textContent === \"h3LOCALE/h3\" ||\n" +
				"            val.textContent === \"h3PARAMETERS/h3\" ||\n" +
				"            val.textContent === \"h3SERVER URL/h3\" ||\n" +
				"            val.textContent === \"h3SHARED STATE/h3\") {\n" +
				"\n" +
				"            val.outerHTML = \"<h3 style='border-bottom: 2px solid black; padding-top: 5px'>\" + val.outerHTML.replace(\"h3\", \"\").replace(\"/h3\", \"\") + \"</h3>\"\n" +
				"        }\n" +
				"        if (val.textContent === \"Key\" || val.textContent === \"Value\") {\n" +
				"            val.innerHTML = \"<h4>\" + val.outerHTML + \"</h4>\"\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    // For Parameters\n" +
				"    const keys = " + paramKeys + "\n" +

				"    let table = '';\n" +
				"    let first = true;\n" +
				"    let very_end = false;\n" +
				"    let trimmed;\n" +
				"    //Loops through divs\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        //iterates through keys in list to find place I want to make table\n" +
				"        for (key in keys) {\n" +
				"\n" +
				"            if (val.textContent.startsWith(keys[key])) {\n" +
				"                //First time coming into loop so this is the beginning\n" +
				"                if (first === true) {\n" +
				"                    table += \"<table data-testid id=\"list-resource-table\" role=\"table\" aria-busy=\"false\" aria-colcount=\"3\" class=\"table b-table table-hover\">\n\"" +
				"                    first = false;\n" +
				"                }\n" +
				"                //This is the last key\n" +
				"                else if (val.textContent.startsWith(keys[keys.length - 1])) {\n" +
				"                    const string = val.textContent;\n" +
				"                    trimmed = '';\n" +
				"                    for (letter in string) {\n" +
				"                        if (string[letter] === \"[\") {\n" +
				"                            break;\n" +
				"                        } else {\n" +
				"                            trimmed += string[letter]\n" +
				"                        }\n" +
				"\n" +
				"                    }\n" +
				"                    //Creates final row and closes table tag\n" +
				"                    table += \"<tr><td><code>\" + trimmed + \"</code></td><td>\" + val.textContent.replace(keys[key], \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                } else {\n" +
				"                    //If not first or last it creates the table row and removes that div\n" +
				"                    //If I don't remove the div it still has the original value with the old styling\n" +
				"                    const string = val.textContent;\n" +
				"                    trimmed = '';\n" +
				"                    for (letter in string) {\n" +
				"                        if (string[letter] === \"[\") {\n" +
				"                            break;\n" +
				"                        } else {\n" +
				"                            trimmed += string[letter]\n" +
				"                        }\n" +
				"                    }\n" +
				"                    table += \"<tr><td><code>\" + trimmed + \"</code></td><td>\" + val.textContent.replace(keys[key], \"\") + \"</td></tr>\"\n" +
				"                    //<table class=\"table table-hover\"><thead><tr><th class=\"selection-cell-header\" data-row-selection=\"true\"><div class=\"checkbox\"><input class=\"react-bs-select-all\" id=\"checkboxHeader\" name=\"checkboxHeader\" type=\"checkbox\"><label for=\"checkboxHeader\"></label></div></th><th tabindex=\"0\" aria-label=\"Username sortable\" class=\"sortable\">Username<span class=\"order\"><span class=\"dropdown\"><span class=\"caret\"></span></span><span class=\"dropup\"><span class=\"caret\"></span></span></span></th><th tabindex=\"0\">Full name</th><th tabindex=\"0\">Email address</th><th tabindex=\"0\">Status</th></tr></thead><tbody><tr><td class=\"selection-cell\"><div class=\"checkbox\"><input id=\"checkbox0\" name=\"checkbox0\" type=\"checkbox\"><label for=\"checkbox0\"></label></div></td><td title=\"demo\"><span class=\"am-table-icon-cell\"><span class=\"fa-stack fa-lg am-table-icon-cell-stack\"><i class=\"fa fa-circle fa-stack-2x text-primary\"></i><i class=\"fa fa-address-card fa-stack-1x fa-inverse\"></i></span> <span><span>demo</span></span></span></td><td title=\"demo\"><span>demo</span></td><td><span>demo@example.com</span></td><td><span class=\"text-success\"><i class=\"fa fa-check-circle\"></i> Active</span></td></tr><tr><td class=\"selection-cell\"><div class=\"checkbox\"><input id=\"checkbox1\" name=\"checkbox1\" type=\"checkbox\"><label for=\"checkbox1\"></label></div></td><td title=\"test_iproov\"><span class=\"am-table-icon-cell\"><span class=\"fa-stack fa-lg am-table-icon-cell-stack\"><i class=\"fa fa-circle fa-stack-2x text-primary\"></i><i class=\"fa fa-address-card fa-stack-1x fa-inverse\"></i></span> <span><span>test_iproov</span></span></span></td><td title=\"test_iproov\"><span>test_iproov</span></td><td><span>testiproov@mailinator.com</span></td><td><span class=\"text-success\"><i class=\"fa fa-check-circle\"></i> Active</span></td></tr></tbody></table></div>\n" +
				"                    document.getElementById(val.id).remove()\n" +
				"                }\n" +
				"                if (very_end) {\n" +
				"                    val.innerHTML = table;\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    function singleItemTable(string) {\n" +
				"        const keys = [\n" +
				"            \"Server URL\",\n" +
				"            \"HostName\",\n" +
				"            \"Preferred Locale\",\n" +
				"            \"ClientIp\",\n" +
				"            \"AuthID\"\n" +
				"        ];\n" +
				"        let table = '';\n" +
				"        let first = true;\n" +
				"        let very_end = false;\n" +
				"        let key = 0;\n" +
				"        for (const val of document.querySelectorAll('div')) {\n" +
				"            if (val.textContent.startsWith(string)) {\n" +
				"                table += \"<div data-v-6fc3383e class=\"mb-0 table-responsive\"><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                if (string === \"HostName\") {\n" +
				"                    table += \"<tr><td><code>\" + \"HostName\" + \"</code></td><td>\" + val.textContent.replace(\"HostName\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"Server URL\") {\n" +
				"                    table += \"<tr><td><code>\" + \"Server URL\" + \"</code></td><td>\" + val.textContent.replace(\"Server URL\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"Preferred Locale\") {\n" +
				"                    table += \"<tr><td><code>\" + \"Preferred Locale\" + \"</code></td><td>\" + val.textContent.replace(\"Preferred Locale\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"ClientIp\") {\n" +
				"                    table += \"<tr><td><code>\" + \"ClientIp\" + \"</code></td><td>\" + val.textContent.replace(\"ClientIp\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                } else if (string === \"AuthID\") {\n" +
				"                    table += \"<tr><td><code>\" + \"AuthID\" + \"</code></td><td>\" + val.textContent.replace(\"AuthID\", \"\") + \"</td></tr></table></div>\"\n" +
				"                    very_end = true;\n" +
				"                    if (very_end) {\n" +
				"                        val.innerHTML = table;\n" +
				"                        break;\n" +
				"                    }\n" +
				"                }\n" +
				"            }\n" +
				"\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    singleItemTable(\"HostName\")\n" +
				"    singleItemTable(\"Server URL\")\n" +
				"    singleItemTable(\"ClientIp\")\n" +
				"    singleItemTable(\"Preferred Locale\")\n" +
				"    singleItemTable(\"AuthID\")\n" +
				"    singleItemTable(\"amlbcookie\")\n" +

				"\n" +
				"\n" +

				"\n" +
				"    const cookies = " + cookies + "\n" +
				"\n" +
				"    let cookies_table = '';\n" +
				"    let first_cookie = true;\n" +
				"    let very_end_cookie = false;\n" +
				"    let trimmed_cookie;\n" +
				"    //Loops through divs\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        if (very_end_cookie) {\n" +
				"            break;\n" +
				"        }\n" +
				"//iterates through keys in list to find place I want to make table\n" +
				"        for (key in cookies) {\n" +
				"            if (val.textContent.startsWith(headers[key]) && first_cookie === true) {\n" +
				"//First time coming into loop so this is the beginning\n" +
				"                if (first_cookie === true) {\n" +
				"                    cookies_table += \"<div data-v-6fc3383e class=\"mb-0 table-responsive\"><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                    first_cookie = false;\n" +
				"                }\n" +
				"                //This is the last key\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_cookie = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_cookie += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"\n" +
				"                cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"            } else if (val.textContent.startsWith(cookies[key])) {\n" +
				"\n" +
				"                //If not first or last it creates the table row and removes that div\n" +
				"                //If I don't remove the div it still has the original value with the old styling\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_cookie = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_cookie += string[letter]\n" +
				"                    }\n" +
				"                }\n" +
				"\n" +
				"                cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"\n" +
				"            } else if (val.textContent.startsWith(cookies[cookies.length - 1])) {\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_cookie = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_cookie += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"                cookies_table += \"<tr><td><code>\" + trimmed_cookie + \"</code></td><td>\" + val.textContent.replace(cookies[key], \"\") + \"</td></tr></table></div>\"\n" +
				"                very_end_cookie = true;\n" +
				"                val.innerHTML = \"\"\n" +
				"                break\n" +
				"\n" +
				"            }\n" +
				"        }\n" +
				"        if (very_end_cookie) {\n" +
				"            val.innerHTML = cookies_table;\n" +
				"            break\n" +
				"        }\n" +
				"    }";

		ScriptTextOutputCallback script = new ScriptTextOutputCallback(javascript);
		callbacks.add(script);

		String test = "\n" +
				"    const headers = " + headerKeys +";\n"+
				"\n" +
				"    let headers_table = '';\n" +
				"    let first_headers = true;\n" +
				"    let very_end_headers = false;\n" +
				"    let trimmed_headers;\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"        if (very_end_headers) {\n" +
				"            break;\n" +
				"        }\n" +
				"\n" +
				"        for (key in headers) {\n" +
				"            if (val.textContent.startsWith(headers[key]) && first_headers === true) {\n" +
				"\n" +
				"                if (first_headers === true) {\n" +
				"                    headers_table += \"<div data-v-6fc3383e class=\"mb-0 table-responsive\"><table class='table table-bordered table-striped table-detail'>\";\n" +
				"                    first_headers = false;\n" +
				"                }\n" +
				"                //This is the last key\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"            } else if (val.textContent.startsWith(headers[key])) {\n" +
				"\n" +
				"                //If not first or last it creates the table row and removes that div\n" +
				"                //If I don't remove the div it still has the original value with the old styling\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"                }\n" +
				"\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr>\"\n" +
				"                val.innerHTML = \"\"\n" +
				"\n" +
				"            } else if (val.textContent.startsWith(headers[headers.length - 1])) {\n" +
				"                const string = val.textContent;\n" +
				"                trimmed_headers = '';\n" +
				"                for (letter in string) {\n" +
				"                    if (string[letter] === \"[\") {\n" +
				"                        break;\n" +
				"                    } else {\n" +
				"                        trimmed_headers += string[letter]\n" +
				"                    }\n" +
				"\n" +
				"                }\n" +
				"                //Creates final row and closes table tag\n" +
				"                headers_table += \"<tr><td><code>\" + trimmed_headers + \"</code></td><td>\" + val.textContent.replace(headers[key], \"\") + \"</td></tr></table></div>\"\n" +
				"                very_end_headers = true;\n" +
				"                break\n" +
				"\n" +
				"            }\n" +
				"        }\n" +
				"        if (very_end_headers) {\n" +
				"            val.innerHTML = headers_table;\n" +
				"            break\n" +
				"        }\n" +
				"    }\n";

		ScriptTextOutputCallback newer = new ScriptTextOutputCallback(test);
		callbacks.add(newer);

		String single = "   function singleItemTable(string) {\n" +
				"\n" +
				"       let table = '';\n" +
				"\n" +
				"       let very_end = false;\n" +
				"\n" +
				"       for (const val of document.querySelectorAll('div')) {\n" +
				"           if (val.textContent.startsWith(string)) {\n" +
				"               table += \"<div data-v-6fc3383e class=\"mb-0 table-responsive\"><table class='table table-bordered table-striped table-detail'>\";\n" +
				"               if (string === \"HostName\") {\n" +
				"                   table += \"<tr><td><code>\" + \"HostName\" + \"</code></td><td>\" + val.textContent.replace(\"HostName\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"Server URL\") {\n" +
				"                   table += \"<tr><td><code>\" + \"Server URL\" + \"</code></td><td>\" + val.textContent.replace(\"Server URL\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"Preferred Locale\") {\n" +
				"                   table += \"<tr><td><code>\" + \"Preferred Locale\" + \"</code></td><td>\" + val.textContent.replace(\"Preferred Locale\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"ClientIP\") {\n" +
				"                   table += \"<tr><td><code>\" + \"ClientIP\" + \"</code></td><td>\" + val.textContent.replace(\"ClientIP\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               } else if (string === \"AuthID\") {\n" +
				"                   table += \"<tr><td><code>\" + \"AuthID\" + \"</code></td><td>\" + val.textContent.replace(\"AuthID\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               }else if (string === \"amlbcookie\") {\n" +
				"                   table += \"<tr><td><code>\" + \"amlbcookie\" + \"</code></td><td>\" + val.textContent.replace(\"amlbcookie\", \"\") + \"</td></tr></table></div>\"\n" +
				"                   very_end = true;\n" +
				"                   if (very_end) {\n" +
				"                       val.innerHTML = table;\n" +
				"                       break;\n" +
				"                   }\n" +
				"               }\n" +
				"           }\n" +
				"\n" +
				"       }\n" +
				"   }\n" +
				"\n" +
				"   singleItemTable(\"HostName\")\n" +
				"   singleItemTable(\"Server URL\")\n" +
				"   singleItemTable(\"ClientIP\")\n" +
				"   singleItemTable(\"Preferred Locale\")\n" +
				"   singleItemTable(\"AuthID\")\n" +
				"   singleItemTable(\"amlbcookie\")\n";
		ScriptTextOutputCallback single_tables = new ScriptTextOutputCallback(single);
		callbacks.add(single_tables);

		String bootstrap = "/*String script = \"var sc = document.createElement('link'); \"\n" +
				"\t\t\t\t\t\t+ \"sc.setAttribute('rel', 'stylesheet');\"\n" +
				"\t\t\t\t\t\t+ \"sc.setAttribute('href', 'https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css');\"\n" +
				"\t\t\t\t\t\t+ \"sc.setAttribute('integrity', 'sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T');\"\n" +
				"\t\t\t\t\t\t+ \"sc.setAttribute('crossorigin', 'anonymous');\"\n" +
				"\t\t\t\t\t\t+ \"document.head.appendChild(sc);\";*/";
	ScriptTextOutputCallback bootstrapClass = new ScriptTextOutputCallback(bootstrap);
	callbacks.add(bootstrapClass);
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
	private static final String html2text(String html) {
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
