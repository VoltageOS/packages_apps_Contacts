/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.list;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Contact list filter parameters.
 */
public final class ContactListFilter implements Comparable<ContactListFilter> {

    public static final int FILTER_TYPE_DEFAULT = -1;
    public static final int FILTER_TYPE_ALL_ACCOUNTS = -2;
    public static final int FILTER_TYPE_CUSTOM = -3;
    public static final int FILTER_TYPE_STARRED = -4;
    public static final int FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY = -5;
    public static final int FILTER_TYPE_SINGLE_CONTACT = -6;

    public static final int FILTER_TYPE_ACCOUNT = 0;
    public static final int FILTER_TYPE_GROUP = 1;

    private static final String KEY_FILTER_TYPE = "filter.type";
    private static final String KEY_ACCOUNT_NAME = "filter.accountName";
    private static final String KEY_ACCOUNT_TYPE = "filter.accountType";
    private static final String KEY_GROUP_ID = "filter.groupId";
    private static final String KEY_GROUP_SOURCE_ID = "filter.groupSourceId";
    private static final String KEY_GROUP_READ_ONLY = "filter.groupReadOnly";

    public int filterType;
    public String accountType;
    public String accountName;
    public Drawable icon;
    public long groupId;
    public String groupSourceId;
    public boolean groupReadOnly;
    public String title;
    private String mId;

    public ContactListFilter(int filterType) {
        this.filterType = filterType;
    }

    public ContactListFilter(
            String accountType, String accountName, Drawable icon, String title) {
        this.filterType = ContactListFilter.FILTER_TYPE_ACCOUNT;
        this.accountType = accountType;
        this.accountName = accountName;
        this.icon = icon;
        this.title = title;
    }

    public ContactListFilter(String accountType, String accountName, long groupId,
            String groupSourceId, boolean groupReadOnly, String title) {
        this.filterType = ContactListFilter.FILTER_TYPE_GROUP;
        this.accountType = accountType;
        this.accountName = accountName;
        this.groupId = groupId;
        this.groupSourceId = groupSourceId;
        this.groupReadOnly = groupReadOnly;
        this.title = title;
    }

    /**
     * Returns true if this filter is based on data and may become invalid over time.
     */
    public boolean isValidationRequired() {
        return filterType == FILTER_TYPE_ACCOUNT || filterType == FILTER_TYPE_GROUP;
    }

    @Override
    public String toString() {
        switch (filterType) {
            case ContactListFilter.FILTER_TYPE_ACCOUNT:
                return "account: " + accountType + " " + accountName;
            case ContactListFilter.FILTER_TYPE_GROUP:
                return "group: " + accountType + " " + accountName + " " + title + "(" + groupId
                        + ")";
        }
        return super.toString();
    }

    @Override
    public int compareTo(ContactListFilter another) {
        int res = accountName.compareTo(another.accountName);
        if (res != 0) {
            return res;
        }

        res = accountType.compareTo(another.accountType);
        if (res != 0) {
            return res;
        }

        if (filterType != another.filterType) {
            return filterType - another.filterType;
        }

        String title1 = title != null ? title : "";
        String title2 = another.title != null ? another.title : "";
        return title1.compareTo(title2);
    }

    @Override
    public int hashCode() {
        int code = filterType;
        if (accountType != null) {
            code = code * 31 + accountType.hashCode();
            code = code * 31 + accountName.hashCode();
        }
        if (groupSourceId != null) {
            code = code * 31 + groupSourceId.hashCode();
        } else if (groupId != 0) {
            code = code * 31 + (int) groupId;
        }
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ContactListFilter)) {
            return false;
        }

        ContactListFilter otherFilter = (ContactListFilter) other;
        if (filterType != otherFilter.filterType
                || !TextUtils.equals(accountName, otherFilter.accountName)
                || !TextUtils.equals(accountType, otherFilter.accountType)) {
            return false;
        }

        if (groupSourceId != null && otherFilter.groupSourceId != null) {
            return groupSourceId.equals(otherFilter.groupSourceId);
        }

        return groupId == otherFilter.groupId;
    }

    public static void storeToPreferences(SharedPreferences prefs, ContactListFilter filter) {
        prefs.edit()
            .putInt(KEY_FILTER_TYPE, filter == null ? FILTER_TYPE_DEFAULT : filter.filterType)
            .putString(KEY_ACCOUNT_NAME, filter == null ? null : filter.accountName)
            .putString(KEY_ACCOUNT_TYPE, filter == null ? null : filter.accountType)
            .putLong(KEY_GROUP_ID, filter == null ? -1 : filter.groupId)
            .putString(KEY_GROUP_SOURCE_ID, filter == null ? null : filter.groupSourceId)
            .putBoolean(KEY_GROUP_READ_ONLY, filter == null ? false : filter.groupReadOnly)
            .apply();
    }

    public static ContactListFilter restoreFromPreferences(SharedPreferences prefs) {
        int filterType = prefs.getInt(KEY_FILTER_TYPE, FILTER_TYPE_DEFAULT);
        if (filterType == FILTER_TYPE_DEFAULT) {
            return null;
        }

        ContactListFilter filter = new ContactListFilter(filterType);
        filter.accountName = prefs.getString(KEY_ACCOUNT_NAME, null);
        filter.accountType = prefs.getString(KEY_ACCOUNT_TYPE, null);
        filter.groupId = prefs.getLong(KEY_GROUP_ID, -1);
        filter.groupSourceId = prefs.getString(KEY_GROUP_SOURCE_ID, null);
        filter.groupReadOnly = prefs.getBoolean(KEY_GROUP_READ_ONLY, false);
        return filter;
    }

    /**
     * Returns a string that can be used as a stable persistent identifier for this filter.
     */
    public String getId() {
        if (mId == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(filterType);
            if (accountType != null) {
                sb.append('-').append(accountType);
            }
            if (accountName != null) {
                sb.append('-').append(accountName.replace('-', '_'));
            }
            if (groupSourceId != null) {
                sb.append('-').append(groupSourceId);
            } else if (groupId != 0) {
                sb.append('-').append(groupId);
            }
            mId = sb.toString();
        }
        return mId;
    }
}
