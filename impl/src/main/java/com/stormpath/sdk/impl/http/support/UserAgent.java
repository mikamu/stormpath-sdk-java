/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.impl.http.support;

import com.stormpath.sdk.lang.Classes;
import com.stormpath.sdk.lang.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is in charge of constructing the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">
 * User-Agent http header</a> string that will be sent to Stormpath in order to describe the current running environment of this Java SDK.
 * <p/>
 * The form of this string is the concatenation of the following sub-items:
 * <ol>
 *     <li>The stormpath integration and version separated by a '/'.  If there is no integration being used, this can be omitted
 *     <li>The stormpath sdk and version separated by a '/'
 *     <li>The runtime information (runtime/version)
 *     <ol type="a">
 *          <li>Integration Runtime (if there is no integration being used, this can be omitted)
 *          <li>SDK Runtime
 *     </ol>
 *     <li>The OS common name and version separated by a '/'.
 *     <li>All other system information included in parentheses
 * </ol>
 * <p/>
 * The User-Agent value is created when this class is loaded. The string can be obtained just by invoking
 * {@link com.stormpath.sdk.impl.http.support.UserAgent#getUserAgentString() UserAgent.getUserAgentString()}.
 * <p/>
 * This is a sample User-Agent string:
 * <i>stormpath-spring-security/0.7.0 stormpath-sdk-java/1.0.0 spring/4.0.4.RELEASE java/1.7.0_45 Mac OS X/10.9.2 (spring-security/3.2.0.RELEASE jetty/8.1.5.v20120716)</i>
 *
 * @since 1.0.RC3
 */
public class UserAgent {

    //Integrations (aka Plugins)
    private static final String INTEGRATION_SHIRO_ID = "stormpath-shiro";
    private static final String INTEGRATION_SHIRO_CLASS = "com.stormpath.shiro.realm.ApplicationRealm";
    private static final String INTEGRATION_SPRING_SECURITY_ID = "stormpath-spring-security";
    private static final String INTEGRATION_SPRING_SECURITY_CLASS = "com.stormpath.spring.security.provider.StormpathAuthenticationProvider";

    //SDK
    private static final String STORMPATH_SDK_STRING = "stormpath-sdk-java";

    //Integration Runtimes
    private static final String INTEGRATION_RUNTIME_SPRING_SECURITY_ID = "spring";
    private static final String INTEGRATION_RUNTIME_SPRING_SECURITY_CLASS = "org.springframework.core.SpringVersion";

    //Runtime
    private static final String SDK_RUNTIME_STRING = "java";

    ////Additional Information////

    //Security Frameworks
    private static final String SECURITY_FRAMEWORK_SHIRO_ID = "shiro";
    private static final String SECURITY_FRAMEWORK_SHIRO_CLASS = "org.apache.shiro.SecurityUtils";
    private static final String SECURITY_FRAMEWORK_SPRING_SECURITY_ID = "spring-security";
    private static final String SECURITY_FRAMEWORK_SPRING_SECURITY_CLASS = "org.springframework.security.core.SpringSecurityCoreVersion";

    //Web Servers
    private static final String WEB_SERVER_TOMCAT_ID = "tomcat";
    private static final String WEB_SERVER_TOMCAT_BOOTSTRAP_CLASS = "org.apache.catalina.startup.Bootstrap";
    private static final String WEB_SERVER_TOMCAT_EMBEDDED_CLASS = "org.apache.catalina.startup.Tomcat";
    private static final String WEB_SERVER_JETTY_ID = "jetty";
    private static final String WEB_SERVER_JETTY_CLASS = "org.eclipse.jetty.servlet.listener.ELContextCleaner";
    private static final String WEB_SERVER_JBOSS_ID = "jboss";
    private static final String WEB_SERVER_JBOSS_CLASS = "org.jboss.as.security.plugins.AuthenticationCacheEvictionListener";
    private static final String WEB_SERVER_WEBSPHERE_ID = "websphere";
    private static final String WEB_SERVER_WEBSPHERE_CLASS = "com.ibm.websphere.product.VersionInfo";
    private static final String WEB_SERVER_GLASSFISH_ID = "glassfish";
    private static final String WEB_SERVER_GLASSFISH_CLASS = "com.sun.enterprise.glassfish.bootstrap.GlassFishMain";
    private static final String WEB_SERVER_WEBLOGIC_ID = "weblogic";
    private static final String WEB_SERVER_WEBLOGIC_CLASS = "weblogic.version";

    private static final String VERSION_SEPARATOR = "/";
    private static final String ENTRY_SEPARATOR = " ";

    //Placeholder for the actual User-Agent String
    private static final String USER_AGENT = createUserAgentString();

    private UserAgent() {
    }

    public static String getUserAgentString() {
        return USER_AGENT;
    }

    private static String createUserAgentString() {
        String userAgent =  getIntegrationString() +
                getStormpathSdkString() +
                getIntegrationRuntimeString() +
                getSDKRuntimeString() +
                getOSString() +
                getAdditionalInformation();
        return userAgent.trim();
    }

    private static String getAdditionalInformation() {
        String additionalInformation;
        additionalInformation = getSecurityFrameworkString() + getWebServerString();
        if(Strings.hasText(additionalInformation)) {
            return "(" + additionalInformation.trim() + ")";
        }
        return "";
    }

    private static String getIntegrationString() {
        String integrationString;
        integrationString = getFullEntryStringUsingPomProperties(INTEGRATION_SHIRO_CLASS, INTEGRATION_SHIRO_ID);
        if(Strings.hasText(integrationString)) {
            return integrationString;
        }
        integrationString = getFullEntryStringUsingPomProperties(INTEGRATION_SPRING_SECURITY_CLASS, INTEGRATION_SPRING_SECURITY_ID);
        if(Strings.hasText(integrationString)) {
            return integrationString;
        }
        return "";
    }

    private static String getStormpathSdkString() {
        return STORMPATH_SDK_STRING + VERSION_SEPARATOR + Version.getClientVersion() + ENTRY_SEPARATOR;
    }

    private static String getIntegrationRuntimeString() {
        String integrationRuntimeString;
        integrationRuntimeString = getFullEntryStringUsingManifest(INTEGRATION_RUNTIME_SPRING_SECURITY_CLASS, INTEGRATION_RUNTIME_SPRING_SECURITY_ID);
        if(Strings.hasText(integrationRuntimeString)) {
            return integrationRuntimeString;
        }
        return "";
    }

    private static String getSDKRuntimeString() {
        return SDK_RUNTIME_STRING + VERSION_SEPARATOR + System.getProperty("java.version") + ENTRY_SEPARATOR;
    }

    private static String getOSString() {
        return System.getProperty("os.name") + VERSION_SEPARATOR + System.getProperty("os.version") + ENTRY_SEPARATOR;
    }

    private static String getSecurityFrameworkString() {
        String securityFrameworkString;
        securityFrameworkString = getFullEntryStringUsingManifest(SECURITY_FRAMEWORK_SHIRO_CLASS, SECURITY_FRAMEWORK_SHIRO_ID);
        if(Strings.hasText(securityFrameworkString)) {
            return securityFrameworkString;
        }
        securityFrameworkString = getFullEntryStringUsingManifest(SECURITY_FRAMEWORK_SPRING_SECURITY_CLASS, SECURITY_FRAMEWORK_SPRING_SECURITY_ID);
        if(Strings.hasText(securityFrameworkString)) {
            return securityFrameworkString;
        }
        return "";
    }

    private static String getWebServerString() {
        String webServerString;
        //Glassfish uses Tomcat internally, therefore the Glassfish check must be carried out before Tomcat's
        webServerString = getFullEntryStringUsingManifest(WEB_SERVER_GLASSFISH_CLASS, WEB_SERVER_GLASSFISH_ID);
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getFullEntryStringUsingManifest(WEB_SERVER_TOMCAT_BOOTSTRAP_CLASS, WEB_SERVER_TOMCAT_ID);
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getFullEntryStringUsingManifest(WEB_SERVER_TOMCAT_EMBEDDED_CLASS, WEB_SERVER_TOMCAT_ID);
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getFullEntryStringUsingManifest(WEB_SERVER_JETTY_CLASS, WEB_SERVER_JETTY_ID);
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getFullEntryStringUsingManifest(WEB_SERVER_JBOSS_CLASS, WEB_SERVER_JBOSS_ID);
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getWebSphereEntryString();
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }
        webServerString = getWebLogicEntryString();
        if(Strings.hasText(webServerString)) {
            return webServerString;
        }

        return "";
    }

    private static String getFullEntryStringUsingPomProperties(String fqcn, String entryId) {
        if (Classes.isAvailable(fqcn)) {
            return entryId + VERSION_SEPARATOR + getVersionInfoFromPomProperties(fqcn) + ENTRY_SEPARATOR;
        }
        return null;
    }

    private static String getFullEntryStringUsingManifest(String fqcn, String entryId) {
        if (Classes.isAvailable(fqcn)) {
            return entryId + VERSION_SEPARATOR + getVersionInfoInManifest(fqcn) + ENTRY_SEPARATOR;
        }
        return null;
    }

    private static String getWebSphereEntryString() {
        if (Classes.isAvailable(WEB_SERVER_WEBSPHERE_CLASS)) {
            return WEB_SERVER_WEBSPHERE_ID + VERSION_SEPARATOR + getWebSphereVersion() + ENTRY_SEPARATOR;
        }
        return null;
    }

    private static String getWebLogicEntryString() {
        if (Classes.isAvailable(WEB_SERVER_WEBLOGIC_CLASS)) {
            return WEB_SERVER_WEBLOGIC_ID + VERSION_SEPARATOR + getWebLogicVersion() + ENTRY_SEPARATOR;
        }
        return null;
    }

    /**
     * WARNING: This method must never be invoked unless we already know that the class identified by the parameter <code>fqcn</code>
     * really exists in the classpath. For example, we first need to assure that <code>Classes.isAvailable(fqcn))</code> is <code>TRUE</code>
     */
    private static String getVersionInfoFromPomProperties(String fqcn) {
        String version = "unknown";
        try{
            Class clazz = Classes.forName(fqcn);
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();

            //Let's remove "jar:file:" from the beginning and also the className
            String jarPath = classPath.subSequence(9, classPath.lastIndexOf("!")).toString();
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> enumeration = jarFile.entries();
            String pomPropertiesPath = null;
            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                if (entry.getName().endsWith("pom.properties")) {
                    pomPropertiesPath = entry.getName();
                    break;
                }
            }
            if (pomPropertiesPath != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream("/" + pomPropertiesPath)));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("version=")) {
                        version = line.split("=")[1];
                        break;
                    }
                }
            }
        } catch (IOException e) {
            //Either the jar file or the internal "pom.properties" file could not be read, there is nothing we can do...
        }
        return version;
    }

    /**
     * WARNING: This method must never be invoked unless we already know that the class identified by the parameter <code>fqcn</code>
     * really exists in the classpath. For example, we first need to assure that <code>Classes.isAvailable(fqcn))</code> is <code>TRUE</code>
     */
    private static String getVersionInfoInManifest(String fqcn){
        //get class package
        Package thePackage = Classes.forName(fqcn).getPackage();
        //examine the package object
        String version = thePackage.getSpecificationVersion();
        if (!Strings.hasText(version)) {
            version = thePackage.getImplementationVersion();
        }
        if(!Strings.hasText(version)) {
            version = "null";
        }
        return version;
    }

    /**
     * This method should only be invoked after already knowing that the class identified by <code>WEB_SERVER_WEBSPHERE_CLASS</code>
     * really exists in the classpath. For example, it can be checked that <code>Classes.isAvailable(WEB_SERVER_WEBSPHERE_CLASS))</code>
     * is <code>TRUE</code>
     */
    private static String getWebSphereVersion() {
        try {
            Class<?> versionClass = Class.forName(WEB_SERVER_WEBSPHERE_CLASS);
            Object versionInfo = versionClass.newInstance();
            Method method = versionClass.getDeclaredMethod("runReport", String.class, PrintWriter.class);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            method.invoke(versionInfo, "", printWriter);
            String version = stringWriter.toString();
            // version looks like this, so we need to "substring" it:
            //
            //
            //IBM WebSphere Product Installation Status Report
            //--------------------------------------------------------------------------------
            //
            //Report at date and time August 13, 2014 1:12:06 PM ART
            //
            //Installation
            //--------------------------------------------------------------------------------
            //Product Directory        C:\Program Files\IBM\WebSphere\AppServer
            //Version Directory        C:\Program Files\IBM\WebSphere\AppServer\properties\version
            //DTD Directory            C:\Program Files\IBM\WebSphere\AppServer\properties\version\dtd
            //Log Directory            C:\Documents and Settings\All Users\Application Data\IBM\Installation Manager\logs
            //
            //Product List
            //--------------------------------------------------------------------------------
            //BASETRIAL                installed
            //
            //Installed Product
            //--------------------------------------------------------------------------------
            //Name                  IBM WebSphere Application Server
            //Version               8.5.5.2
            //ID                    BASETRIAL
            //Build Level           cf021414.01
            //Build Date            4/8/14
            //Package               com.ibm.websphere.BASETRIAL.v85_8.5.5002.20140408_1947
            //Architecture          x86 (32 bit)
            //Installed Features    IBM 32-bit WebSphere SDK for Java
            //WebSphere Application Server Full Profile

            version = version.substring(version.indexOf("Installed Product"));
            version = version.substring(version.indexOf("Version"));
            version = version.substring(version.indexOf(" "), version.indexOf("\n")).trim();
            return version;

        }catch (Exception e) {
            //there was a problem obtaining the WebSphere version
        }
        //returning null so we can identify in the User-Agent String that we are not properly handling some WebSphere version
        return "null";
    }

    /**
     * This method should only be invoked after already knowing that the class identified by <code>WEB_SERVER_WEBLOGIC_CLASS</code>
     * really exists in the classpath. For example, it can be checked that <code>Classes.isAvailable(WEB_SERVER_WEBLOGIC_CLASS))</code>
     * is <code>TRUE</code>
     */
    private static String getWebLogicVersion() {
        try {
            Class<?> versionClass = Class.forName(WEB_SERVER_WEBLOGIC_CLASS);
            Object version = versionClass.newInstance();
            Method method = versionClass.getDeclaredMethod("getReleaseBuildVersion");
            return (String) method.invoke(version);
        }catch (Exception e) {
            //there was a problem obtaining the WebLogic version
        }
        //returning null so we can identify in the User-Agent String that we are not properly handling some WebLogic version
        return "null";
    }

}
