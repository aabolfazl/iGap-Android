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

import net.iGap.observers.interfaces.OnUserProfileCheckUsername;
import net.iGap.proto.ProtoError;
import net.iGap.proto.ProtoUserProfileCheckUsername;

public class UserProfileCheckUsernameResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public Object identity;

    public UserProfileCheckUsernameResponse(int actionId, Object protoClass, Object identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override
    public void handler() {
        super.handler();
        ProtoUserProfileCheckUsername.UserProfileCheckUsernameResponse.Builder builder = (ProtoUserProfileCheckUsername.UserProfileCheckUsernameResponse.Builder) message;
        if (identity instanceof OnUserProfileCheckUsername) {
            ((OnUserProfileCheckUsername) identity).OnUserProfileCheckUsername(builder.getStatus());
        } else {
            throw new ClassCastException("identity must be : " + OnUserProfileCheckUsername.class.getName());
        }
    }

    @Override
    public void timeOut() {
        super.timeOut();
    }

    @Override
    public void error() {
        super.error();

        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();
        if (identity instanceof OnUserProfileCheckUsername) {
            ((OnUserProfileCheckUsername) identity).Error(majorCode, minorCode);
        } else {
            throw new ClassCastException("identity must be : " + OnUserProfileCheckUsername.class.getName());
        }
    }
}


