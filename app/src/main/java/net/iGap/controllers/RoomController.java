package net.iGap.controllers;

import net.iGap.G;
import net.iGap.helper.FileLog;
import net.iGap.module.accountManager.AccountManager;
import net.iGap.network.IG_RPC;
import net.iGap.observers.eventbus.EventManager;
import net.iGap.realm.RealmChannelRoom;
import net.iGap.realm.RealmRoom;

public class RoomController extends BaseController {

    private static volatile RoomController[] instance = new RoomController[AccountManager.MAX_ACCOUNT_COUNT];
    private String TAG = getClass().getSimpleName();

    public static RoomController getInstance(int account) {
        RoomController localInstance = instance[account];
        if (localInstance == null) {
            synchronized (RoomController.class) {
                localInstance = instance[account];
                if (localInstance == null) {
                    instance[account] = localInstance = new RoomController(account);
                }
            }
        }
        return localInstance;
    }

    public RoomController(int currentAccount) {
        super(currentAccount);
    }

    public void ChannelUpdateReactionStatus(long roomId, boolean status) {

        IG_RPC.Channel_Update_Reaction_Status req = new IG_RPC.Channel_Update_Reaction_Status();
        req.roomId = roomId;
        req.reactionStatus = status;

        getRequestManager().sendRequest(req, (response, error) -> {
            if (response != null) {
                IG_RPC.Res_Channel_Update_Reaction_Status res = (IG_RPC.Res_Channel_Update_Reaction_Status) response;
                RealmChannelRoom.updateReactionStatus(res.roomId, res.reactionStatus);
                G.runOnUiThread(() -> getEventManager().postEvent(EventManager.CHANNEL_UPDATE_VOTE, res.roomId, res.reactionStatus));
            } else {
                IG_RPC.Error e = new IG_RPC.Error();
                FileLog.e("Delete Message -> Major" + e.major + "Minor" + e.minor);
            }
        });
    }

    public void ChannelUpdateSignature(long roomId, boolean signature) {

        IG_RPC.Channel_Update_Signature req = new IG_RPC.Channel_Update_Signature();
        req.roomId = roomId;
        req.signature = signature;

        getRequestManager().sendRequest(req, (response, error) -> {
            if (response != null) {
                IG_RPC.Res_Channel_Update_Signature res = (IG_RPC.Res_Channel_Update_Signature) response;
                RealmRoom.updateSignature(res.roomId, res.signature);
                G.runOnUiThread(() -> getEventManager().postEvent(EventManager.CHANNEL_UPDATE_SIGNATURE, res.roomId, res.signature));
            } else {
                IG_RPC.Error e = new IG_RPC.Error();
                FileLog.e("Delete Message -> Major" + e.major + "Minor" + e.minor);
            }
        });
    }

}
