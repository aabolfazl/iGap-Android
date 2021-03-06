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

import net.iGap.G;
import net.iGap.helper.LooperThreadHelper;
import net.iGap.realm.RealmClientCondition;
import net.iGap.request.RequestClientCondition;

public class ClientConditionResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;
    public static int retryCount = 0;

    public ClientConditionResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override
    public void handler() {
        super.handler();
        retryCount = 0;
        RealmClientCondition.clearOfflineAction();
        if (G.onClientCondition != null) {
            G.onClientCondition.onClientCondition();
        }
    }

    @Override
    public void timeOut() {
        super.timeOut();
        /**
         * timeOut call error also
         */
    }

    @Override
    public void error() {
        super.error();

        if (retryCount < 10) {
            retryCount++;
            LooperThreadHelper.getInstance().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new RequestClientCondition().clientCondition(RealmClientCondition.computeClientCondition(null));
                }
            }, retryCount * 300);
        }

        if (G.onClientCondition != null) {
            G.onClientCondition.onClientConditionError();
        }
    }
}


