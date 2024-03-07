/*
 * This code is to be used exclusively in connection with Ping Identity Corporation software or services.
 * Ping Identity Corporation only offers such software or services to legal entities who have entered
 * into a binding license agreement with Ping Identity Corporation.
 */


package com.marketplace.helper;

import static com.marketplace.helper.DisplayDebug.DisplayDebugOutcome.NEXT_OUTCOME;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang.text.StrSubstitutor;
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

/**
 * Display Debug Node
 */
@Node.Metadata(outcomeProvider = DisplayDebug.DisplayDebugOutcomeProvider.class, configClass = DisplayDebug.Config.class)
public class DisplayDebug extends AbstractDecisionNode {
	private final Logger logger = LoggerFactory.getLogger(DisplayDebug.class.getName());
	private final Config config;
	private static final String BUNDLE = DisplayDebug.class.getName();
	private String loggerPrefix = "[DisplayDebug Node][Marketplace] ";

	private static final String onPrem_sdkJsPathTemplate = "com/marketplace/helper/on-prem.js";
	private final String clientScript;

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
		@Attribute(order = 55)
		default boolean back2back() {
			return false;
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
		this.clientScript = readJS();
	}

	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		ArrayList<Callback> callbacks = new ArrayList<Callback>();
		NodeState ns = context.getStateFor(this);
		Integer counter;
		if(config.display() && config.pretty() && config.back2back()){
			JsonValue jsonCounter = ns.get("counter");
			counter = jsonCounter.asInteger();

		}else{
			counter = 0;
		}

		if(config.display()) {
				try {
					callbacks = new ArrayList<Callback>();

					List<StringAttributeInputCallback> stringCallbacks = context.getCallbacks(StringAttributeInputCallback.class);
					if (context.hasCallbacks()) {
						logger.debug(loggerPrefix + "Done.");
						ns = context.getStateFor(this);

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
							StringAttributeInputCallback stringCallback; // used for text boxes

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
							on_prem_flag = true;
						}
						else if(headers.containsValue("forgerock-sdk")){
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

						//displayhtml_OnPrem(callbacks, paramKeys, headerKeys, cookies, counter);
						ScriptTextOutputCallback scb = getScriptedCallback(headerKeys, paramKeys, cookies);
						callbacks.add(scb);
					}
					else if(config.pretty() && cloud_flag){
						displayhtml_Cloud(callbacks, paramKeys, headerKeys, cookies);
					}

					if(config.display() && config.pretty() && config.back2back()) {
						counter++;
						ns.putShared("counter", counter);
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



		String[] callbackoptions = {"Next","Cancel"};
		ConfirmationCallback cc = new ConfirmationCallback(ConfirmationCallback.INFORMATION,callbackoptions, 0);
		callbacks.add(cc);
		return Action.goTo(NEXT_OUTCOME.name()).build();
	}

	private ScriptTextOutputCallback getScriptedCallback(List<String> headers, List<String> parameters, List<String> cookies) {
		String parameters_string = parameters.toString();
		parameters_string = parameters_string.replace("\"","\\\"");

		String headers_string = headers.toString();
		headers_string = headers_string.replace("\"","\\\"");

		String cookies_string = cookies.toString();
		cookies_string = cookies_string.replace("\"","\\\"");


		Map<String, String> sdkConfigMap = new HashMap<>();
		sdkConfigMap.put("parameters", parameters_string); // Put the parameters list value where "parameters" are in the js file
		sdkConfigMap.put("cookies", cookies_string); // Put the cookies list value where "cookies" are in the js file
		sdkConfigMap.put("headers", headers_string);// Put the headers list value where "headers" are in the js file

		String sdkJs = new StrSubstitutor(sdkConfigMap).replace(this.clientScript); // This replaces the values in the this.clientScript (The js file) with the java values

		ScriptTextOutputCallback callback = new ScriptTextOutputCallback(sdkJs);
		return callback;
	}

	private String readJS() throws NodeProcessException {
		URL resource = Resources.getResource(onPrem_sdkJsPathTemplate);
		try {
			return Resources.toString(resource, Charsets.UTF_8);
		} catch (IOException ex) {
			logger.error(String.format("%s %s: %s", loggerPrefix, ex.getClass().getCanonicalName(),
					Arrays.toString(ex.getStackTrace())));
			throw new NodeProcessException(ex);
		}
	}

	public void displayhtml_Cloud(ArrayList<Callback> callbacks, List<String> paramKeys, List<String> headerKeys, List<String> cookies){



		String javascript = "(async function onLoad() {\n" +
				"    //\n" +
				"    //\n" +
				"    let first_header = true\n" +
				"     let table = ''\n" +
				"    let headers_end = false\n" +
				"\n" +
				"\n" +
				"    const parameters = "+paramKeys+"\n" +
				"\n" +
				"\n" +
				"    function trimmer(string){\n" +
				"      let trimmed = ''\n" +
				"      let letter = ''\n" +
				"      if(string.startsWith(\"ClientIp\") || string.startsWith(\"HostName\") || string.startsWith(\"AuthID\")){\n" +
				"        for(letter in string){\n" +
				"          if(string[letter] === \" \"){\n" +
				"            break\n" +
				"          }\n" +
				"          else{\n" +
				"            trimmed += string[letter]\n" +
				"          }\n" +
				"        }\n" +
				"       } else if(string.startsWith(\"Preferred Locale\") || string.startsWith(\"Server URL\")){\n" +
				"        let space_counter = 0;\n" +
				"        for(letter in string){\n" +
				"          if(space_counter === 2){\n" +
				"            space_counter = 0\n" +
				"            break\n" +
				"          }\n" +
				"          if(string[letter] === \" \"){\n" +
				"            trimmed += string[letter]\n" +
				"            space_counter += 1\n" +
				"          } else{\n" +
				"            trimmed += string[letter]\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"      return trimmed;\n" +
				"    }\n" +
				"\n" +
				"\n" +
				"\n" +
				"    let headers_table = '';\n" +
				"    let first_headers = true;\n" +
				"    let very_end_headers = false;\n" +
				"    let trimmed_headers;\n" +
				"\n" +
				"    let params_table = '';\n" +
				"    let first_param = true;\n" +
				"    let very_end_params = false;\n" +
				"    let trimmed_params;\n" +
				"    let key = '';\n" +
				"    //For single value tables\n" +
				"     for (const val of document.querySelectorAll('div')) {\n" +
				"\n" +
				"       if(val.className === \"card border-xs-0 border-sm d-flex fr-stretch-card\"){\n" +
				"         val.style = \"width: fit-content\"\n" +
				"       }\n" +
				"\n" +
				"       if(val.className === \"min-vh-100 d-flex flex-column fr-fullscreen-mobile\"){\n" +
				"         val.style = \"margin: 0px\"\n" +
				"       }\n" +
				"\n" +
				"\n" +
				"       let string = val.textContent.trimStart(\" \").trimEnd(\" \")\n" +
				"\n" +
				"       let trimmer_val;\n" +
				"       //Create tables for values that will not change\n" +
				"       if (string.startsWith(\"AuthID\")) {\n" +
				"         trimmer_val = trimmer(string);\n" +
				"         table += '<table style=\"width: 100%\">'\n" +
				"         table += '<tr><td  style=\"overflow-wrap: anywhere; border: 1px solid black;\"><code>' + trimmer_val + '</code></td><td style=\" padding: 20px; border: 1px solid black; overflow-wrap: anywhere\">' + val.textContent.replace(\"AuthID\", \"\") + '</td></tr></table>'\n" +
				"         // style=\"border: 1px solid black; text-align: left\" style=\"overflow-wrap: anywhere\"\n" +
				"         trimmer_val = ''\n" +
				"         val.innerHTML = table\n" +
				"         table = ''\n" +
				"       } else if (string.startsWith(\"ClientIp\")) {\n" +
				"         val.style = \"display: flex; justify-content: left\"\n" +
				"         trimmer_val = trimmer(string);\n" +
				"         val.className = \"display: block; margin-left: auto; margin-right: auto; width: 40%\"\n" +
				"         table += '<table style=\"width: 100%\">'\n" +
				"         table += '<tr style=\\\"border: 1px solid black; text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black; padding: 20px;\\n text-align: left\\\" style=\"overflow-wrap: anywhere\"><code>' + trimmer_val + '</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\"overflow-wrap: anywhere\">' + val.textContent.replace(\"ClientIp\", \"\") + '</td></tr></table>'\n" +
				"         val.innerHTML = table\n" +
				"         table = ''\n" +
				"       } else if (string.startsWith(\"Server URL\")) {\n" +
				"         val.style = \"display: flex; justify-content: left\"\n" +
				"         trimmer_val = trimmer(string)\n" +
				"         table += '<table style=\"width: 100%\">'\n" +
				"         table += '<tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\"overflow-wrap: anywhere\"><code>' + trimmer_val + '</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\"overflow-wrap: anywhere\">' + string.replace(\"Server URL\", \"\") + '</td></tr></table>'\n" +
				"         val.innerHTML = table\n" +
				"         table = ''\n" +
				"       } else if (string.startsWith(\"Preferred Locale\")) {\n" +
				"         val.style = \"display: flex; justify-content: left\"\n" +
				"         trimmer_val = trimmer(string)\n" +
				"         table += '<table style=\"width: 100%\">'\n" +
				"         table += '<tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\"overflow-wrap: anywhere\"><code>' + trimmer_val + '</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\"overflow-wrap: anywhere\">' + string.replace(\"Preferred Locale\", \"\") + '</td></tr></table>'\n" +
				"         val.innerHTML = table\n" +
				"         table = ''\n" +
				"       } else if (string.startsWith(\"HostName\")) {\n" +
				"         val.style = \"display: flex; justify-content: left\"\n" +
				"         trimmer_val = trimmer(string)\n" +
				"         table += '<table style=\"width: 100%\">'\n" +
				"         table += '<tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\"overflow-wrap: anywhere\"><code>' + trimmer_val + '</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\"overflow-wrap: anywhere\">' + string.replace(\"HostName\", \"\") + '</td></tr></table>'\n" +
				"         val.innerHTML = table\n" +
				"         table = ''\n" +
				"       }\n" +
				"\n" +
				"\n" +
				"       //Create h3 tags for headers\n" +
				"       if (string === \"h3NODE STATE/h3\" ||\n" +
				"               string === \"h3AUTHID/h3\" ||\n" +
				"               string === \"h3HEADERS/h3\" ||\n" +
				"               string === \"h3CLIENT IP/h3\" ||\n" +
				"               string === \"h3COOKIES/h3\" ||\n" +
				"               string === \"h3HOSTNAME/h3\" ||\n" +
				"               string === \"h3LOCALE/h3\" ||\n" +
				"               string === \"h3PARAMETERS/h3\" ||\n" +
				"               string === \"h3SERVER URL/h3\" ||\n" +
				"               string === \"h3SHARED STATE/h3\") {\n" +
				"         val.outerHTML = \"<h3 style='padding-top: 50px'>\" + val.outerHTML.replace(\"h3\", \"\").replace(\"/h3\", \"\") + \"</h3>\"\n" +
				"       }\n" +
				"       //Create h4 tags for Key: Value pairs\n" +
				"       if (string === \"Key\" || string === \"Value\") {\n" +
				"         val.outerHTML = \"<h4>\" + val.outerHTML + \"</h4>\"\n" +
				"       }\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"     }\n" +
				"\n" +
				"  let inner = 0;\n" +
				"  let letter = ''\n" +
				"    //For Parameters\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"      inner = inner + 1\n" +
				"      if (inner === 2) {\n" +
				"        let string = val.textContent.trimStart(\" \").trimEnd(\" \")\n" +
				"\n" +
				"      let trimmed = ''\n" +
				"        for (key in parameters) {\n" +
				"          if (string.startsWith(parameters[key])) {\n" +
				"\n" +
				"\n" +
				"            if(string.startsWith(\"realm:\")){\n" +
				"              continue\n" +
				"            }\n" +
				"\n" +
				"            for(letter in string){\n" +
				"              if(string[letter] === \" \"){\n" +
				"                break\n" +
				"              }\n" +
				"              else{\n" +
				"                trimmed += string[letter]\n" +
				"              }\n" +
				"            }\n" +
				"\n" +
				"            if(first_param){\n" +
				"\n" +
				"              params_table += \"<table style='width: 100%'><tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\\\"overflow-wrap: anywhere\\\"><code>\"+ trimmed+\"</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\">\"+string.replace(trimmed, \"\")+\"</td></tr>\"\n" +
				"              first_param = false\n" +
				"              val.innerHTML = \"\"\n" +
				"            }\n" +
				"            else if(string.startsWith(parameters[parameters.length-1])){\n" +
				"              params_table +=\" <tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\\\"overflow-wrap: anywhere\\\"><code>\"+trimmed+\"</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\\\"overflow-wrap: anywhere\\\">\"+string.replace(trimmed, \"\")+\"</td></tr>\"+\"</table>\"\n" +
				"              very_end_params = true\n" +
				"              val.innerHTML = \"\"\n" +
				"            }\n" +
				"            else{\n" +
				"              params_table += \"<tr style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px; \\\"><td style=\\\"border: 1px solid black;\\n text-align: left; padding: 20px;\\\" style=\\\"overflow-wrap: anywhere\\\"><code>\"+trimmed+\"</code></td><td style=\\\"border: 1px solid black;\\n text-align: left\\\" style=\\\"overflow-wrap: anywhere\\\">\"+string.replace(trimmed, \"\")+\"</td></tr>\"\n" +
				"              val.innerHTML = \"\"\n" +
				"            }\n" +
				"\n" +
				"\n" +
				"\n" +
				"          }\n" +
				"        }\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"    }else{\n" +
				"      continue;\n" +
				"      }\n" +
				"      inner = 0\n" +
				"\n" +
				"\n" +
				"\n" +
				"    if(very_end_params){\n" +
				"      val.innerHTML = params_table\n" +
				"      very_end_params = false\n" +
				"    }\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"    }\n" +
				"\n" +
				"    //For Cookies\n" +
				"    let cookies_table = '';\n" +
				"    let first_cookie = true;\n" +
				"    let very_end_cookie = false;\n" +
				"    let trimmed_cookie;\n" +
				"    const cookies ="+cookies+"\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"      let string = val.textContent.trimStart(\" \").trimEnd(\" \")\n" +
				"      if (very_end_cookie) {\n" +
				"        break;\n" +
				"      }\n" +
				"\n" +
				"//iterates through keys in list to find place I want to make table\n" +
				"      //console.log(string)\n" +
				"      for (key in cookies) {\n" +
				"\n" +
				"        if(string.startsWith(cookies[key])) {\n" +
				"\n" +
				"//First time coming into loop so this is the beginning\n" +
				"            trimmed_cookie = '';\n" +
				"            for (letter in string) {\n" +
				"              if (string[letter] === \" \") {\n" +
				"                break;\n" +
				"              } else {\n" +
				"                trimmed_cookie += string[letter]\n" +
				"              }\n" +
				"\n" +
				"            }\n" +
				"          if (first_cookie) {\n" +
				"            cookies_table += '<table style=\"width: 100%\"><tr style=\"border: 1px solid black;\\n text-align: left; overflow-wrap: anywhere; padding: 20px;\"\"><td style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"  style=\"overflow-wrap: anywhere\"><code>'+trimmed_cookie+'</code></td><td style=\"border: 1px solid black;\\n text-align: left\" style=\"overflow-wrap: anywhere\">'+string.replace(trimmed_cookie, \"\")+'</td></tr>';\n" +
				"\n" +
				"            first_cookie = false;\n" +
				"\n" +
				"            //This is the last key\n" +
				"\n" +
				"            //Creates final row and closes table tag\n" +
				"            val.innerHTML = \"\"\n" +
				"\n" +
				"            if (cookies.length === 1) {\n" +
				"              cookies_table += \"</table>\"\n" +
				"              very_end_cookie = true\n" +
				"              break\n" +
				"            }\n" +
				"          }\n" +
				"        } else if (string.startsWith(cookies[key])) {\n" +
				"          //If not first or last it creates the table row and removes that div\n" +
				"          //If I don't remove the div it still has the original value with the old styling\n" +
				"          trimmed_cookie = '';\n" +
				"          for (letter in string) {\n" +
				"            if (string[letter] === \" \") {\n" +
				"              break;\n" +
				"            } else {\n" +
				"              trimmed_cookie += string[letter]\n" +
				"            }\n" +
				"          }\n" +
				"\n" +
				"          cookies_table += '<tr style=\"border: 1px solid black; text-align: left; padding: 20px;\"><td style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\" style=\"overflow-wrap: anywhere\"><code>' + trimmed_cookie + '</code></td><td style=\"border: 1px solid black;\\n text-align: left\" style=\"overflow-wrap: anywhere\">' + string.replace(trimmed_cookie, \"\") + '</td></tr>'\n" +
				"\n" +
				"          val.innerHTML = \"\"\n" +
				"\n" +
				"        }\n" +
				"\n" +
				"        else if (string.startsWith(cookies[cookies.length - 1])) {\n" +
				"\n" +
				"          trimmed_cookie = '';\n" +
				"          for (letter in string) {\n" +
				"            if (string[letter] === \" \") {\n" +
				"              break;\n" +
				"            } else {\n" +
				"              trimmed_cookie += string[letter]\n" +
				"            }\n" +
				"\n" +
				"          }\n" +
				"          //Creates final row and closes table tag\n" +
				"          cookies_table +='<tr style=\"border: 1px solid black; text-align: left; padding: 20px;\"><td style=\"overflow-wrap: anywhere; padding: 20px;\" style=\"border: 1px solid black; text-align: left\"><code>' + trimmed_cookie + '</code></td><td style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\" style=\"overflow-wrap: anywhere\">' + string.replace(trimmed_cookie, \"\") + '</td></tr></table>'\n" +
				"          very_end_cookie = true;\n" +
				"          val.innerHTML = \"\"\n" +
				"          break\n" +
				"\n" +
				"        }\n" +
				"      }\n" +
				"      if (very_end_cookie) {\n" +
				"        val.innerHTML = cookies_table;\n" +
				"\n" +
				"        break\n" +
				"      }\n" +
				"    }\n" +
				"\n" +
				"\n" +
				"    const headers = "+headerKeys+ "\n" +
				"    let here = true\n" +
				"  //For Headers Table\n" +
				"    for (const val of document.querySelectorAll('div')) {\n" +
				"      inner = inner + 1\n" +
				"      if (inner === 2) {\n" +
				"        let trimmed = ''\n" +
				"        let string = val.textContent.trimStart(\" \").trimEnd(\" \")\n" +
				"        for(letter in string){\n" +
				"          if(string[letter] === \" \" || string[letter] === \"[\"){\n" +
				"            break\n" +
				"          }else{\n" +
				"            trimmed += string[letter]\n" +
				"          }\n" +
				"        }\n" +
				"        let value = trimmed.trim()\n" +
				"\n" +
				"\n" +
				"          for (key in headers) {\n" +
				"\n" +
				"            if (value === headers[key]) {\n" +
				"              if (first_header) {\n" +
				"                headers_table += '<table style = \"border: 1px solid black; padding-top: 0px; width: 100%\" >'\n" +
				"                headers_table += '<tr style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"><td style=\"border: 1px solid black; padding: 20px;\\n\"><code>' + value + '</code></td><td style=\"border: 1px solid black;\\n text-align: left\">' + string.replace(value, \"\") + '</td></tr>'\n" +
				"                first_header = false\n" +
				"                val.innerHTML = \"\"\n" +
				"              } else if (value === headers[headers.length - 1]) {\n" +
				"                headers_table += '<tr style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"><td style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"><code>' + value + '</code></td><td style=\"border: 1px solid black;\\n text-align: left\">' + string.replace(value, \"\") + '</td></tr></table>'\n" +
				"                val.innerHTML = \"\"\n" +
				"                very_end_headers = true\n" +
				"              } else {\n" +
				"                headers_table += '<tr style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"><td style=\"border: 1px solid black;\\n text-align: left; padding: 20px;\"><code>' + value + '</code></td><td style=\"border: 1px solid black;\\n text-align: left\">' + string.replace(value, \"\") + '</td></tr>'\n" +
				"                val.remove()\n" +
				"              }\n" +
				"            }\n" +
				"\n" +
				"          }\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"        if(very_end_headers){\n" +
				"          if(here){\n" +
				"            val.innerHTML = headers_table\n" +
				"            headers_end = false\n" +
				"            here = false\n" +
				"          }\n" +
				"        }\n" +
				"      }else{\n" +
				"        continue;\n" +
				"      }\n" +
				"\n" +
				"\n" +
				"      inner = 0\n" +
				"    }\n" +
				"\n" +
				"\n" +
				"  })();";
		ScriptTextOutputCallback script = new ScriptTextOutputCallback(javascript);
		callbacks.add(script);
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
