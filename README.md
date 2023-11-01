<!--
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
 * Copyright 2022 ForgeRock AS.
-->
# DisplayDebug

A simple authentication node for ForgeRock's [Identity Platform][forgerock_platform] 7.2.0 and above. This node is used for debugging a user journey. In the admin configuration, the admin is able to edit items in the shared state while seeing what values are the AuthID, HTTP Request Headers, ClientIP, Cookies, Hostname, Locales, Parameters, and the Server URL. 


Copy the .jar file from the ../target directory into the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed.  Restart the web container to pick up the new node.  The node will then appear in the authentication trees components palette.


**USAGE HERE**

In the admin configuration, "Display" allows the admin to either display that node or to not display the node. So the admin does not have to delete that node out of the journey when they don't need it or re-add it into the journey when they do. Uncheck the buttons to choose whether or not to display that data or not (e.g. If the admin unchecks the "Cookies" button; the cookies will not be displayed or if the admin chooses to uncheck the Display button; the node is not displayed). There are 3 Key: Value text boxes. Insert in a key value in the "Key" text box and a value in the "Value" in the Value text box to insert a Key: Value pair into the Shared State. Also has the capability to edit the values of realm, authId, and username values stored in the Shared State. 

The code in this repository has binary dependencies that live in the ForgeRock maven repository. Maven can be configured to authenticate to this repository by following the following [ForgeRock Knowledge Base Article](https://backstage.forgerock.com/knowledge/kb/article/a74096897).

**BUILD INSTRUCTIONS**
Go to directory where Node is stored (e.g. ~/Repositories/display-debug) run "mvn clean install" to create .jar file for the Display Debug node. The created .jar file should be in the "target" directory (e.g. ~/Repositories/display-debug/target) copy that created jar file and put it into your tomcat/webapps/openam/WEB-INF/lib directory where the other .jar files are located. 
**SCREENSHOTS ARE GOOD LIKE BELOW**

![ScreenShot](./example.png)

        
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. ForgeRock disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.

[forgerock_platform]: https://www.forgerock.com/platform/  
