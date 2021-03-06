/*
 * This is the source code of iGap for Android
 * It is licensed under GNU AGPL v3.0
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright © 2017 , iGap - www.iGap.net
 * iGap Messenger | Free, Fast and Secure instant messaging application
 * The idea of the Kianiranian Company - www.kianiranian.com
 * All rights reserved.
 */

package net.iGap.response;

import net.iGap.module.accountManager.AccountManager;
import net.iGap.module.accountManager.DbManager;
import net.iGap.G;
import net.iGap.proto.ProtoUserAvatarAdd;
import net.iGap.realm.RealmAvatar;

import io.realm.Realm;

public class UserAvatarAddResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public UserAvatarAddResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);
        this.message = protoClass;
        this.identity = identity;
        this.actionId = actionId;
    }

    @Override
    public void handler() {
        super.handler();

        final ProtoUserAvatarAdd.UserAvatarAddResponse.Builder userAvatarAddResponse = (ProtoUserAvatarAdd.UserAvatarAddResponse.Builder) message;

        new Thread(new Runnable() {
            @Override
            public void run() {
                DbManager.getInstance().doRealmTransaction(realm -> {
                    RealmAvatar.putOrUpdate(realm, AccountManager.getInstance().getCurrentUser().getId(), userAvatarAddResponse.getAvatar());
                });
            }
        }).start();

        G.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (G.onUserAvatarResponse != null) {
                    G.onUserAvatarResponse.onAvatarAdd(userAvatarAddResponse.getAvatar());
                }

                if (G.onUserInfoMyClient != null) {
                    G.onUserInfoMyClient.onUserInfoMyClient();
                }
            }
        }, 1000);
    }

    @Override
    public void timeOut() {
        super.timeOut();
        if (G.onUserAvatarResponse != null) {
            G.onUserAvatarResponse.onAvatarAddTimeOut();
        }
    }

    @Override
    public void error() {
        super.error();
        if (G.onUserAvatarResponse != null) {
            G.onUserAvatarResponse.onAvatarError();
        }
    }
}


