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
package com.stormpath.sdk.account;

import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.api.ApiKeyCriteria;
import com.stormpath.sdk.api.ApiKeyList;
import com.stormpath.sdk.api.ApiKeyOptions;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.sdk.directory.Directory;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupCriteria;
import com.stormpath.sdk.group.GroupList;
import com.stormpath.sdk.group.GroupMembership;
import com.stormpath.sdk.group.GroupMembershipList;
import com.stormpath.sdk.provider.ProviderData;
import com.stormpath.sdk.resource.Deletable;
import com.stormpath.sdk.resource.Extendable;
import com.stormpath.sdk.resource.Resource;
import com.stormpath.sdk.resource.Saveable;
import com.stormpath.sdk.tenant.Tenant;

import java.util.Map;

/**
 * An Account is a unique identity within a {@link Directory}.  Accounts within a {@link Directory} or {@link Group}
 * mapped to an {@link com.stormpath.sdk.application.Application Application} may log in to that Application.
 *
 * @since 0.1
 */
public interface Account extends Resource, Saveable, Deletable, Extendable {

    /**
     * Returns the account's username, guaranteed to be unique for all accounts within a Directory.  If you do not have
     * need of a username, it is best to set the username to equal the {@link #getEmail()}.
     *
     * @return the account's username, guaranteed to be unique for all accounts within a Directory.
     */
    String getUsername();

    /**
     * Sets the account's username, which must be unique among all other accounts within a Directory.  If you do not
     * have need of a username, it is best to set the username to equal the {@link #getEmail()}.
     *
     * <p>An attempt to set a username that is in use when creating or saving the account will result in a
     * {@link com.stormpath.sdk.error.Error Error}</p>
     *
     * @param username the account's username, which must be unique among all other accounts within a Directory.
     * @return this instance for method chaining.
     */
    Account setUsername(String username);

    /**
     * Returns the account's email address, guaranteed to be unique for all accounts within a Directory.
     *
     * @return the account's email address, guaranteed to be unique for all accounts within a Directory.
     */
    String getEmail();

    /**
     * Sets the account's email address, which must be unique among all other accounts within a Directory.
     *
     * <p>An attempt to set an email that is in use when creating or saving the account will result in a
     * {@link com.stormpath.sdk.error.Error Error}</p>
     *
     * @param email the account's email address, which must be unique among all other accounts within a Directory.
     * @return this instance for method chaining.
     */
    Account setEmail(String email);

    /**
     * Sets (changes) the account's password to the specified raw (plaintext) password.  ONLY call this method if you
     * legitimately want to set the account password directly.  It is usually more advisable to use Stormpath's
     * account password reset email workflow (when possible).
     *
     * <p>After calling this method, you must call {@link #save()} to propagate the change to the Stormpath
     * servers.</p>
     *
     * @param password the account's new raw (plaintext) password.
     * @return this instance for method chaining.
     */
    Account setPassword(String password);

    /**
     * Returns the account's given name (aka 'first name' in Western cultures').
     *
     * @return the account's given name (aka 'first name' in Western cultures')
     */
    String getGivenName();

    /**
     * Sets the account's given name (aka 'first name' in Western cultures').
     *
     * @param givenName the account's given name (aka 'first name' in Western cultures').
     * @return this instance for method chaining.
     */
    Account setGivenName(String givenName);

    /**
     * Returns the account's middle name(s).
     *
     * @return the account's middle name(s).
     */
    String getMiddleName();

    /**
     * Sets the account's middle name(s).
     *
     * @param middleName the account's middle name(s).
     * @return this instance for method chaining.
     */
    Account setMiddleName(String middleName);

    /**
     * Returns the account's surname (aka 'last name' in Western cultures).
     *
     * @return the account's surname (aka 'last name' in Western cultures).
     */
    String getSurname();

    /**
     * Sets the account's surname (aka 'last name' in Western cultures).
     *
     * @param surname the account's surname (aka 'last name' in Western cultures).
     * @return this instance for method chaining.
     */
    Account setSurname(String surname);

    /**
     * Returns the account's 'full name', per Western cultural conventions.
     *
     * <p>This is <em>NOT</em> a persistent/settable/queryable property.  It is a convenience computed property
     * combining the {@link #getGivenName() givenName} (aka 'first name' in Western cultures) followed by the
     * {@link #getMiddleName() middleName} (if any) followed by the {@link #getSurname() surname} (aka 'last name' in
     * Western cultures).</p>
     *
     * @return the account's 'full name', per Western cultural conventions.
     * @since 0.8
     */
    String getFullName();

    /**
     * Returns the account's status.  Accounts that are not {@link AccountStatus#ENABLED ENABLED} may not login to
     * applications.
     *
     * @return the account's status.
     */
    AccountStatus getStatus();

    /**
     * Sets the account's status.  Accounts that are not {@link AccountStatus#ENABLED ENABLED} may not login to
     * applications.
     *
     * @param status the account's status.
     * @return this instance for method chaining.
     */
    Account setStatus(AccountStatus status);

    /**
     * Returns a paginated list of the account's assigned groups.
     *
     * <p>Tip: If this list might be large, instead of iterating over all groups, it might be more convenient (and
     * practical) to execute a search for one or more of the account's groups using the
     * {@link #getGroups(com.stormpath.sdk.group.GroupCriteria)} or {@link #getGroups(java.util.Map)} methods instead
     * of this one.</p>
     *
     * @return a paginated list of all groups assigned to the Account.
     * @see #getGroups(com.stormpath.sdk.group.GroupCriteria)
     * @see #getGroups(java.util.Map)
     */
    GroupList getGroups();

    /**
     * Returns a paginated list of the account's assigned groups that match the specified query criteria.
     *
     * <p>This method is mostly provided as a non-type-safe alternative to the
     * {@link #getGroups(com.stormpath.sdk.group.GroupCriteria)} method which might be useful in dynamic languages on
     * the
     * JVM (for example, with Groovy):
     * <pre>
     * def groups = account.getGroups([description: '*foo*', orderBy: 'name desc', limit: 50])
     * </pre>
     * The query parameter names and values must be equal to those documented in the Stormpath REST API product guide.
     * <p/>
     * Each {@code queryParams} key/value pair will be converted to String name to String value pairs and appended to
     * the resource URL as query parameters, for example:
     * <pre>
     * .../accounts/accountId/groups?param1=value1&param2=value2&...
     * </pre>
     * </p>
     * If in doubt, use {@link #getGroups(com.stormpath.sdk.group.GroupCriteria)} as all possible query options are
     * available
     * via type-safe guarantees that can be auto-completed by most IDEs.
     *
     * @param queryParams the query parameters to use when performing a request to the collection.
     * @return a paginated list of the account's groups that match the specified query criteria.
     * @since 0.8
     */
    GroupList getGroups(Map<String, Object> queryParams);

    /**
     * Returns a paginated list of the account's assigned groups that match the specified query criteria.  The
     * {@link com.stormpath.sdk.group.Groups Groups} utility class is available to help construct
     * the criteria DSL - most modern IDEs can auto-suggest and auto-complete as you type, allowing for an easy
     * query-building experience.  For example:
     * <pre>
     * account.getGroups(Groups.where(
     *     Groups.description().containsIgnoreCase("foo"))
     *     .and(Groups.name().startsWithIgnoreCase("bar"))
     *     .orderByName()
     *     .orderByStatus().descending()
     *     .withAccounts(10, 10)
     *     .offsetBy(20)
     *     .limitTo(25));
     * </pre>
     * or, if you use static imports:
     * <pre>
     * import static com.stormpath.sdk.group.Groups.*;
     *
     * ...
     *
     * account.getGroups(where(
     *      description().containsIgnoreCase("foo"))
     *     .and(name().startsWithIgnoreCase("bar"))
     *     .orderByName()
     *     .orderByStatus().descending()
     *     .withAccounts(10, 10)
     *     .offsetBy(20)
     *     .limitTo(25));
     * </pre>
     *
     * @param criteria the criteria to use when performing a request to the collection.
     * @return a paginated list of the account's groups that match the specified query criteria.
     * @since 0.8
     */
    GroupList getGroups(GroupCriteria criteria);

    /**
     * Returns the account's parent Directory (where the account is stored).
     *
     * @return the account's parent Directory (where the account is stored)
     */
    Directory getDirectory();

    /* NOT YET ENABLED (focusing on collection searches first)
     *
     * Returns the account's parent Directory according to the specified options (providing for resource
     * expansion).
     * <p/>
     * The {@link com.stormpath.sdk.directory.Directories Directories} utility class is available to help construct
     * the options DSL.  For example:
     * <pre>
     * account.getDirectory(Directories.options().withAccounts(50, 100));
     * </pre>
     *
     * @param options the retrieval options to use when performing a request to acquire the Directory.
     * @return the account's Directory.
     * @since 0.8
     *
    Directory getDirectory(DirectoryOptions options);
    */

    /**
     * Returns the Stormpath Tenant that owns this Account resource.
     *
     * @return the Stormpath Tenant that owns this Account resource.
     */
    Tenant getTenant();

    /**
     * Returns all {@link GroupMembership}s that reflect this account.  This method is an alternative to
     * {@link #getGroups()} that returns the actual association entity representing the account and a group.
     *
     * @since 0.4
     */
    GroupMembershipList getGroupMemberships();

    /**
     * Assigns this account to the specified Group.
     *
     * <p><b>Immediate Execution:</b> Unlike other Account methods, you do <em>not</em> need to call {@link #save()}
     * afterwards. This method will interact with the server immediately.</p>
     *
     * @return the new GroupMembership resource created reflecting the account-to-group association.
     * @since 0.4
     */
    GroupMembership addGroup(Group group);

    /**
     * Returns the account's email verification token.  This will only be non-null if the Account holder has been asked
     * to verify their email account by clicking a link in an email.
     *
     * @return the account's email verification token.
     */
    EmailVerificationToken getEmailVerificationToken();

    /**
     * Saves this Account resource and ensures the returned Account response reflects the specified options.  This
     * enhances performance by 'piggybacking' the response to return related resources you know you will use after
     * saving the account.
     *
     * @param responseOptions The {@code AccountOptions} to use to customize the Account resource returned in the save
     *                        response.
     * @return this instance for method chaining.
     * @since 0.9
     */
    Account saveWithResponseOptions(AccountOptions responseOptions);

    /**
     * Returns true if the account belongs to a group whose name or href is (case insensitive) equal to the specified
     * hrefOrName value, false otherwise.
     *
     * @param hrefOrName the href or name of the group being sought.
     * @return true if the account belongs to a group whose name or href is (case insensitive) equal to the specified
     *         hrefOrName value, false otherwise.
     * @since 0.9.3
     */
    boolean isMemberOfGroup(String hrefOrName);

    /**
     * Returns the ProviderData Resource belonging to the account.
     *
     * @return the ProviderData Resource belonging to the account.
     * @since 1.0.beta
     */
    ProviderData getProviderData();

    /**
     * Returns all {@link ApiKey}s that belong to this account.  The account can use any of these
     * ApiKeys (that are {@link com.stormpath.sdk.api.ApiKey#getStatus() enabled}) to communicate with your
     * Application's API (e.g. REST API). See {@link com.stormpath.sdk.application.Application#authenticateApiRequest(Object)
     * application.authenticateApiRequest(request)} and
     * {@link com.stormpath.sdk.application.Application#authenticateOauthRequest(Object)
     * application.authenticateOauthRequest(request)} for more information.
     *
     * @return all {@link ApiKey}s that belong to this account.
     * @since 1.0.RC
     */
    ApiKeyList getApiKeys();

    /**
     * Returns a paginated list of the account's api keys that match the specified query criteria.  The account can use
     * any of these ApiKeys (that are {@link com.stormpath.sdk.api.ApiKey#getStatus() enabled}) to communicate with your
     * Application's API (e.g. REST API). See {@link com.stormpath.sdk.application.Application#authenticateApiRequest(Object)
     * application.authenticateApiRequest(request)} and
     * {@link com.stormpath.sdk.application.Application#authenticateOauthRequest(Object)
     * application.authenticateOauthRequest(request)} for more information.
     *
     * <p>This method is mostly provided as a non-type-safe alternative to the
     * {@link #getApiKeys(com.stormpath.sdk.api.ApiKeyCriteria)} method which might be useful in dynamic languages on
     * the
     * JVM (for example, with Groovy):
     * <pre>
     * def apiKeys = account.getApiKeys([expand: 'tenant'])
     * </pre>
     * The query parameter names and values must be equal to those documented in the Stormpath REST API product guide.
     * <p/>
     * Each {@code queryParams} key/value pair will be converted to String name to String value pairs and appended to
     * the resource URL as query parameters, for example:
     * <pre>
     * .../accounts/accountId/apiKeys?param1=value1&param2=value2&...
     * </pre>
     * </p>
     * If in doubt, use {@link #getApiKeys(com.stormpath.sdk.api.ApiKeyCriteria)} as all possible query options are
     * available via type-safe guarantees that can be auto-completed by most IDEs.
     *
     * @param queryParams the query parameters to use when performing a request to the collection.
     * @return a paginated list of the account's api keys that match the specified query criteria.
     * @since 1.0.RC
     */
    ApiKeyList getApiKeys(Map<String, Object> queryParams);

    /**
     * Returns a paginated list of the account's api keys that match the specified query criteria.  The
     * {@link com.stormpath.sdk.api.ApiKeys ApiKeys} utility class is available to help construct the criteria DSL -
     * most modern IDEs can auto-suggest and auto-complete as you type, allowing for an easy query-building experience.
     * For example:
     * <pre>
     * account.getApiKeys(ApiKeys.criteria().offsetBy(50).withTenant());
     * </pre>
     * or, if you use static imports:
     * <pre>
     * import static com.stormpath.sdk.api.ApiKeys.*;
     *
     * ...
     *
     * account.getApiKeys(criteria().offsetBy(50).withTenant());
     * </pre>
     *
     * @param criteria the criteria to use when performing a request to the collection.
     * @return a paginated list of the account's api keys that match the specified query criteria.
     * @since 1.0.RC
     */
    ApiKeyList getApiKeys(ApiKeyCriteria criteria);

    /**
     * Creates a new {@link ApiKey} assigned to this account in the Stormpath server and returns the created resource.
     * The account can then use the ApiKey to communicate with your Application's API (e.g. REST API).
     * See {@link com.stormpath.sdk.application.Application#authenticateApiRequest(Object)
     * application.authenticateApiRequest(request)} and
     * {@link com.stormpath.sdk.application.Application#authenticateOauthRequest(Object)
     * application.authenticateOauthRequest(request)} for more information.
     *
     * <h3>Security Notice</h3>
     *
     * <p>The returned ApiKey's {@link com.stormpath.sdk.api.ApiKey#getSecret() secret} is considered
     * <em>extremely</em> sensitive.  Stormpath strongly recommends that you make newly generated ApiKeys accessible
     * to your accounts via a File download that stays secure on their file system.
     * </p>
     *
     * @return the newly created {@link ApiKey}.
     * @since 1.0.RC
     */
    ApiKey createApiKey();

    /**
     * Creates a new {@link ApiKey} assigned to this account in the Stormpath server and ensures the returned
     * {@link ApiKey} reflects the specified {@link ApiKeyOptions}.
     * The account can then use the ApiKey to communicate with your Application's API (e.g. REST API).
     * See {@link com.stormpath.sdk.application.Application#authenticateApiRequest(Object)
     * application.authenticateApiRequest(request)} and
     * {@link com.stormpath.sdk.application.Application#authenticateOauthRequest(Object)
     * application.authenticateOauthRequest(request)} for more information.
     *
     * <h3>Security Notice</h3>
     *
     * <p>The returned ApiKey's {@link com.stormpath.sdk.api.ApiKey#getSecret() secret} is considered
     * <em>extremely</em> sensitive.  Stormpath strongly recommends that you make newly generated ApiKeys accessible
     * to your accounts via a File download that stays secure on their file system.
     * </p>
     *
     * @param options The {@link ApiKeyOptions} to use to customize the ApiKey resource returned in the create
     *                response.
     * @since 1.0.RC
     */
    ApiKey createApiKey(ApiKeyOptions options);
}
