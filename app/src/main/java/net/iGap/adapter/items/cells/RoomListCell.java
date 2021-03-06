package net.iGap.adapter.items.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import net.iGap.G;
import net.iGap.R;
import net.iGap.adapter.items.chat.AbstractMessage;
import net.iGap.helper.HelperCalander;
import net.iGap.helper.HelperImageBackColor;
import net.iGap.helper.HelperLogMessage;
import net.iGap.helper.LayoutCreator;
import net.iGap.helper.avatar.AvatarHandler;
import net.iGap.helper.avatar.ParamWithInitBitmap;
import net.iGap.libs.Tuple;
import net.iGap.libs.emojiKeyboard.emoji.EmojiManager;
import net.iGap.module.AppUtils;
import net.iGap.module.CircleImageView;
import net.iGap.module.FontIconTextView;
import net.iGap.module.Theme;
import net.iGap.module.accountManager.AccountManager;
import net.iGap.module.customView.CheckBox;
import net.iGap.module.customView.TextBadge;
import net.iGap.proto.ProtoGlobal;
import net.iGap.realm.RealmRegisteredInfo;
import net.iGap.realm.RealmRoom;
import net.iGap.realm.RealmRoomMessage;

import java.util.ArrayList;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static net.iGap.adapter.items.chat.ViewMaker.setTextSize;
import static net.iGap.adapter.items.chat.ViewMaker.setTypeFace;
import static net.iGap.proto.ProtoGlobal.Room.Type.CHAT;
import static net.iGap.proto.ProtoGlobal.Room.Type.GROUP;
import static net.iGap.proto.ProtoGlobal.RoomMessageWallet.Type.BILL;
import static net.iGap.proto.ProtoGlobal.RoomMessageWallet.Type.CARD_TO_CARD;
import static net.iGap.proto.ProtoGlobal.RoomMessageWallet.Type.MONEY_TRANSFER;
import static net.iGap.proto.ProtoGlobal.RoomMessageWallet.Type.PAYMENT;
import static net.iGap.proto.ProtoGlobal.RoomMessageWallet.Type.TOPUP;

public class RoomListCell extends FrameLayout {

    public final int FILE = 0x1F4CE;
    public final int VIDEO = 0x1F4F9;
    public final int MUSIC = 0x1F3A7;
    public final int IMAGE = 0x1F5BC;
    public final int GIF = 0x1F308;
    public final int WALLET = 0x1F4B3;

    private AppCompatTextView roomNameTv;
    private FontIconTextView verifyIconTv;
    private CircleImageView avatarImageView;
    private TextView messageDataTv;
    private FontIconTextView muteIconTv;
    private FontIconTextView chatIconTv;
    private AppCompatTextView lastMessageTv;
    private TextBadge badgeView;
    private FontIconTextView statusTv;
    private AppCompatImageView pinView;
    private AppCompatImageView pinCornerView;
    private CheckBox checkBox;

    private boolean haveAvatar = false;
    private boolean isMute = false;
    private boolean haveDate = false;
    private boolean haveName = false;
    private boolean roomVerified = false;
    private boolean haveChatIcon = false;
    private boolean isRtl = G.isAppRtl;
    private boolean haveLastMessage = false;
    private boolean haveBadge = false;
    private boolean haveStatus = false;
    private boolean havePin = false;
    private boolean haveCheckBox = false;

    public RoomListCell(@NonNull Context context) {
        super(context);
    }

    public void setData(RealmRoom room, AvatarHandler avatarHandler, boolean isSelectedMode) {
        if (room.isPinned()) {
            if (!havePin) {
                pinView = new AppCompatImageView(getContext());
                pinCornerView = new AppCompatImageView(getContext());
                pinView.setBackgroundResource(R.drawable.pin);
                pinCornerView.setBackground(Theme.getInstance().tintDrawable(getResources().getDrawable(R.drawable.pin_corner), getContext(), R.attr.colorAccent));

                if (isRtl)
                    pinCornerView.setRotationY(180);

                addView(pinView, 0);
                addView(pinCornerView, 1);
                havePin = true;
            }
        } else if (havePin) {
            removeView(pinView);
            removeView(pinCornerView);
            havePin = false;
        }

        if (!haveAvatar) {
            avatarImageView = new CircleImageView(getContext());
            addView(avatarImageView);
            haveAvatar = true;
            final boolean isMyCloud;

            isMyCloud = room.getChatRoom() != null && room.getChatRoom().getPeerId() > 0 && room.getChatRoom().getPeerId() == AccountManager.getInstance().getCurrentUser().getId();

            if (isMyCloud) {
                avatarHandler.removeImageViewFromHandler(avatarImageView);
                avatarImageView.setImageResource(R.drawable.ic_cloud_space_blue);

            } else {
                avatarHandler(room, avatarHandler);
            }
        } else {
            final boolean isMyCloud;

            isMyCloud = room.getChatRoom() != null && room.getChatRoom().getPeerId() > 0 && room.getChatRoom().getPeerId() == AccountManager.getInstance().getCurrentUser().getId();

            if (isMyCloud) {
                avatarHandler.removeImageViewFromHandler(avatarImageView);
                avatarImageView.setImageResource(R.drawable.ic_cloud_space_blue);

            } else {
                avatarHandler(room, avatarHandler);
            }
        }

        if (haveName) {
            roomNameTv.setText(EmojiManager.getInstance().replaceEmoji(room.getTitle(), roomNameTv.getPaint().getFontMetricsInt(), -1, false));
        }

        if (room.getType() == ProtoGlobal.Room.Type.CHANNEL && room.getChannelRoom().isVerified() || room.getType() == CHAT && room.getChatRoom().isVerified()) {
            if (!roomVerified) {
                verifyIconTv = new FontIconTextView(getContext());
                verifyIconTv.setTextColor(getContext().getResources().getColor(R.color.verify_color));
                verifyIconTv.setText(R.string.verify_icon);
                setTextSize(verifyIconTv, R.dimen.standardTextSize);
                addView(verifyIconTv);
                roomVerified = true;
            }
        } else if (roomVerified) {
            removeView(verifyIconTv);
            roomVerified = false;
        }

        if (room.getLastMessage() != null && room.getLastMessage().getUpdateOrCreateTime() != 0 && !haveDate) {
            messageDataTv = new AppCompatTextView(getContext());
            messageDataTv.setSingleLine(true);
            messageDataTv.setTextColor(Theme.getInstance().getSendMessageTextColor(messageDataTv.getContext()));
            messageDataTv.setText(HelperCalander.getTimeForMainRoom(room.getLastMessage().getUpdateOrCreateTime()));
            setTextSize(messageDataTv, R.dimen.smallTextSize);
            setTypeFace(messageDataTv);
            addView(messageDataTv);
            haveDate = true;

        } else if (room.getLastMessage() != null && room.getLastMessage().getUpdateOrCreateTime() != 0) {
            messageDataTv.setText(HelperCalander.getTimeForMainRoom(room.getLastMessage().getUpdateOrCreateTime()));
        }

        if (room.getMute()) {
            if (!isMute) {
                muteIconTv = new FontIconTextView(getContext());
                muteIconTv.setText(R.string.mute_icon);
                muteIconTv.setGravity(Gravity.RIGHT);
                muteIconTv.setTextColor(Theme.getInstance().getSendMessageTextColor(getContext()));
                setTextSize(muteIconTv, R.dimen.dp13);
                addView(muteIconTv);
                isMute = true;

            }
        } else if (isMute) {
            removeView(muteIconTv);
            isMute = false;
        }

        if (room.getType() == ProtoGlobal.Room.Type.CHANNEL || room.getType() == ProtoGlobal.Room.Type.GROUP) {
            if (!haveChatIcon) {
                chatIconTv = new FontIconTextView(getContext());
                setTextSize(chatIconTv, R.dimen.dp14);
                addView(chatIconTv);
                chatIconTv.setTextColor(Theme.getInstance().getSendMessageTextColor(chatIconTv.getContext()));
                haveChatIcon = true;

            }

            if (room.getType() == ProtoGlobal.Room.Type.CHANNEL)
                chatIconTv.setText(R.string.channel_main_icon);
            else if (room.getType() == ProtoGlobal.Room.Type.GROUP)
                chatIconTv.setText(R.string.group_icon);

        } else if (haveChatIcon) {
            removeView(chatIconTv);
            haveChatIcon = false;
        }

        if (room.getTitle() != null && !haveName) {
            roomNameTv = new AppCompatTextView(getContext());
            roomNameTv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.main_font_bold));
            setTextSize(roomNameTv, R.dimen.dp14);
            roomNameTv.setSingleLine(true);
            roomNameTv.setEllipsize(TextUtils.TruncateAt.END);
            roomNameTv.setText(EmojiManager.getInstance().replaceEmoji(room.getTitle(), roomNameTv.getPaint().getFontMetricsInt(), -1, false));
            roomNameTv.setTextColor(Theme.getInstance().getSendMessageTextColor(roomNameTv.getContext()));
            roomNameTv.setGravity(isRtl ? Gravity.RIGHT : Gravity.LEFT | Gravity.CENTER_VERTICAL);
            addView(roomNameTv);
            haveName = true;
        }

        if (room.getLastMessage() != null) {
            if (!haveLastMessage) {
                lastMessageTv = new AppCompatTextView(getContext());
                lastMessageTv.setEllipsize(TextUtils.TruncateAt.END);
                lastMessageTv.setGravity(isRtl ? Gravity.RIGHT : Gravity.LEFT | Gravity.CENTER_VERTICAL);
                lastMessageTv.setSingleLine(true);
                setTypeFace(lastMessageTv);
                setTextSize(lastMessageTv, R.dimen.dp13);
                addView(lastMessageTv);
                haveLastMessage = true;
            }
            getLastMessage(room, lastMessageTv);
        } else if (haveLastMessage) {
            removeView(lastMessageTv);
            haveLastMessage = false;
        }

        if (!(room.getUnreadCount() < 1)) {
            if (!haveBadge) {
                badgeView = new TextBadge(getContext());
                setTypeFace(badgeView.getTextView());
                addView(badgeView);
                haveBadge = true;
            }
            if (room.getMute()) {
                badgeView.setBadgeColor(getResources().getColor(R.color.gray_9d));
            } else {
                badgeView.setBadgeColor(Theme.getInstance().getAccentColor(badgeView.getContext()));
            }
            badgeView.setText(getUnreadCount(room.getUnreadCount()));
        } else if (haveBadge) {
            removeView(badgeView);
            haveBadge = false;
        }

        if (haveLastMessage && room.getLastMessage().isAuthorMe()) {
            if (!haveStatus) {
                statusTv = new FontIconTextView(getContext());
                setTextSize(statusTv, R.dimen.xlargeTextSize);
                addView(statusTv);
                haveStatus = true;
            }
            ProtoGlobal.RoomMessageStatus status = ProtoGlobal.RoomMessageStatus.UNRECOGNIZED;
            if (room.getLastMessage().getStatus() != null) {
                try {
                    status = ProtoGlobal.RoomMessageStatus.valueOf(room.getLastMessage().getStatus());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            AppUtils.rightMessageStatus(statusTv, status, room.getLastMessage().isAuthorMe());
        } else if (haveStatus) {
            removeView(statusTv);
            haveStatus = false;
        }

        if (isSelectedMode) {
            if (!haveCheckBox) {
                checkBox = new CheckBox(getContext(), R.drawable.round_check);
                checkBox.setVisibility(VISIBLE);
                addView(checkBox);
                haveCheckBox = true;
            }
        } else if (haveCheckBox) {
            removeView(checkBox);
            haveCheckBox = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int h2 = getMeasuredHeight() / 2;
        int standardMargin = dpToPx(8);
        int smallMargin = dpToPx(4);
        int paddingEnd = dpToPx(8);

        if (haveAvatar) {
            int avatarHeight = dpToPx(60);
            avatarImageView.measure(makeMeasureSpec(avatarHeight, MeasureSpec.EXACTLY), makeMeasureSpec(avatarHeight, MeasureSpec.EXACTLY));

            int avatarTop = (getMeasuredHeight() - avatarImageView.getMeasuredHeight()) / 2;
            int avatarBottom = avatarTop + avatarImageView.getMeasuredHeight();

            int avatarLeft = isRtl ? getWidth() - avatarHeight : standardMargin;
            int avatarRight = isRtl ? getWidth() - standardMargin : avatarImageView.getMeasuredHeight();

            avatarImageView.layout(avatarLeft, avatarTop, avatarRight, avatarBottom);

            if (haveName) {
                int roomNameHeight = LayoutCreator.getTextHeight(roomNameTv);
                int roomTop = h2 - roomNameHeight;
                int nameLeft = 0;
                int nameRight = 0;
                roomNameTv.measure(makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), makeMeasureSpec(roomNameHeight, AT_MOST));

                if (haveChatIcon) {
                    int chatIconWidth = LayoutCreator.getTextWidth(chatIconTv);
                    int chatIconHeight = LayoutCreator.getTextHeight(chatIconTv);

                    int t = (roomNameHeight - chatIconHeight) / 2;
                    int chatIconTop = roomTop + t;

                    int chatIconLeft = isRtl ? avatarLeft - chatIconWidth - standardMargin : avatarRight + standardMargin;
                    int chatIconRight = isRtl ? avatarLeft - standardMargin : avatarRight + chatIconWidth + standardMargin;

                    chatIconTv.measure(makeMeasureSpec(chatIconWidth, AT_MOST), makeMeasureSpec(chatIconWidth, AT_MOST));
                    chatIconTv.layout(chatIconLeft, chatIconTop, chatIconRight, h2 - t);

                    if (isRtl) {
                        nameRight = chatIconLeft - smallMargin;
                    } else {
                        nameLeft = chatIconRight + smallMargin;
                    }
                } else {
                    if (isRtl) {
                        nameRight = avatarLeft - standardMargin;
                    } else {
                        nameLeft = avatarRight + standardMargin;
                    }
                }

                if (haveDate) {
                    int dateWidth = LayoutCreator.getTextWidth(messageDataTv);
                    int dateHeight = LayoutCreator.getTextHeight(messageDataTv);
                    int dateTop = h2 - dateHeight;

                    int dateRight = isRtl ? dateWidth + paddingEnd : getWidth() - paddingEnd;
                    int dateLeft = isRtl ? paddingEnd : getWidth() - dateWidth - paddingEnd;

                    messageDataTv.measure(makeMeasureSpec(dateWidth, AT_MOST), makeMeasureSpec(dateHeight, AT_MOST));
                    messageDataTv.layout(dateLeft, dateTop, dateRight, h2);

                    if (roomVerified) {
                        int verifyWidth = LayoutCreator.getTextWidth(verifyIconTv);

                        int t = (dateHeight - verifyWidth) / 2;
                        int verifyTop = dateTop + t;

                        int verifyRight = isRtl ? dateRight + verifyWidth + smallMargin : dateLeft - smallMargin;
                        int verifyLeft = isRtl ? dateRight + smallMargin : dateLeft - verifyWidth - smallMargin;

                        verifyIconTv.measure(makeMeasureSpec(verifyWidth, AT_MOST), makeMeasureSpec(verifyWidth, AT_MOST));
                        verifyIconTv.layout(verifyLeft, verifyTop, verifyRight, h2 - t);

                        if (isMute) {
                            int muteWidth = LayoutCreator.getTextWidth(muteIconTv);

                            int muteTop = dateTop + t;

                            int muteRight = isRtl ? verifyRight + muteWidth + smallMargin : verifyLeft - smallMargin;
                            int muteLeft = isRtl ? verifyRight + smallMargin : verifyLeft - muteWidth - smallMargin;

                            muteIconTv.measure(makeMeasureSpec(muteWidth, AT_MOST), makeMeasureSpec(muteWidth, AT_MOST));
                            muteIconTv.layout(muteLeft, muteTop, muteRight, h2 - t);

                            if (isRtl)
                                nameLeft = muteRight + smallMargin;
                            else
                                nameRight = muteLeft - smallMargin;
                        } else {
                            if (isRtl)
                                nameLeft = verifyRight + smallMargin;
                            else
                                nameRight = verifyLeft - smallMargin;
                        }
                    } else {
                        if (isMute) {
                            int muteWidth = LayoutCreator.getTextWidth(muteIconTv);

                            int muteRight = isRtl ? dateRight + muteWidth + smallMargin : dateLeft - smallMargin;
                            int muteLeft = isRtl ? dateRight + smallMargin : dateLeft - muteWidth - smallMargin;

                            int t = (dateHeight - muteWidth) / 2;
                            int muteTop = dateTop + t;


                            muteIconTv.measure(makeMeasureSpec(muteWidth, AT_MOST), makeMeasureSpec(muteWidth, AT_MOST));
                            muteIconTv.layout(muteLeft, muteTop, muteRight, h2 - t);

                            if (isRtl)
                                nameLeft = muteRight + smallMargin;
                            else
                                nameRight = muteLeft - smallMargin;
                        } else {
                            if (isRtl)
                                nameLeft = dateRight + smallMargin;
                            else
                                nameRight = dateLeft - smallMargin;
                        }
                    }
                } else {
                    if (!isRtl)
                        nameRight = getWidth() - paddingEnd;
                }
                roomNameTv.layout(nameLeft, roomTop, nameRight, h2);
            }

            if (haveLastMessage) {
                int lastMessageHeight = LayoutCreator.getTextHeight(lastMessageTv);

                int messageLeft = isRtl ? paddingEnd : avatarRight + standardMargin;
                int messageRight = isRtl ? avatarLeft - standardMargin : getWidth() - paddingEnd;
                int messageBottom = h2 + lastMessageHeight;

                if (haveBadge) {
                    int badgeHeight = LayoutCreator.getTextHeight(badgeView.getTextView());
                    int badgeWidth = LayoutCreator.getTextWidth(badgeView.getTextView());

                    int badgeRight = isRtl ? paddingEnd + badgeWidth + standardMargin : getWidth() - paddingEnd;
                    int badgeLeft = isRtl ? paddingEnd : getWidth() - badgeWidth - paddingEnd - standardMargin;

                    badgeView.measure(makeMeasureSpec(badgeWidth, AT_MOST), makeMeasureSpec(badgeHeight, AT_MOST));
                    badgeView.layout(badgeLeft, h2 + dpToPx(2), badgeRight, messageBottom - dpToPx(2));

                    if (isRtl) {
                        messageLeft = badgeRight + standardMargin;
                    } else {
                        messageRight = badgeLeft - standardMargin;
                    }

                    lastMessageTv.measure(makeMeasureSpec(messageRight - messageLeft, MeasureSpec.EXACTLY), makeMeasureSpec(lastMessageHeight, AT_MOST));
                    lastMessageTv.layout(messageLeft, h2, messageRight, messageBottom);
                } else if (haveStatus) {
                    int statusHeight = LayoutCreator.getTextHeight(statusTv);
                    int statusWidth = LayoutCreator.getTextWidth(statusTv);

                    int statusRight = isRtl ? paddingEnd + statusWidth + standardMargin : getWidth() - paddingEnd;
                    int statusLeft = isRtl ? paddingEnd : getWidth() - statusWidth - paddingEnd - standardMargin;

                    statusTv.measure(makeMeasureSpec(statusWidth, AT_MOST), makeMeasureSpec(statusHeight, AT_MOST));
                    statusTv.layout(statusLeft, h2 + 6, statusRight, messageBottom);

                    if (isRtl) {
                        messageLeft = statusRight + standardMargin;
                    } else {
                        messageRight = statusLeft - standardMargin;
                    }

                    lastMessageTv.measure(makeMeasureSpec(messageRight - messageLeft, MeasureSpec.EXACTLY), makeMeasureSpec(lastMessageHeight, AT_MOST));
                    lastMessageTv.layout(messageLeft, h2, messageRight, messageBottom);

                } else {
                    lastMessageTv.measure(makeMeasureSpec(messageRight - messageLeft, MeasureSpec.EXACTLY), makeMeasureSpec(lastMessageHeight, AT_MOST));
                    lastMessageTv.layout(messageLeft, h2, messageRight, messageBottom);
                }
            }

            if (havePin) {
                pinView.measure(makeMeasureSpec(getWidth(), AT_MOST), makeMeasureSpec(getHeight(), EXACTLY));
                pinCornerView.measure(makeMeasureSpec(dpToPx(15), AT_MOST), makeMeasureSpec(dpToPx(15), AT_MOST));
                if (isRtl) {
                    pinView.layout(smallMargin, smallMargin, getWidth() - avatarImageView.getWidth() / 2, getHeight() - smallMargin);
                    pinCornerView.layout(pinView.getLeft(), pinView.getTop(), pinView.getLeft() + dpToPx(15), pinView.getTop() + dpToPx(15));
                } else {
                    pinView.layout(avatarImageView.getWidth() / 2, smallMargin, getWidth() - smallMargin, getHeight() - smallMargin);
                    pinCornerView.layout(pinView.getRight() - dpToPx(15), pinView.getTop(), pinView.getRight(), pinView.getTop() + dpToPx(15));
                }
            }

            if (haveCheckBox) {
                checkBox.measure(makeMeasureSpec(dpToPx(20), EXACTLY), makeMeasureSpec(dpToPx(20), EXACTLY));
                checkBox.layout(isRtl ? avatarLeft : avatarRight - dpToPx(20), avatarBottom - dpToPx(20), isRtl ? avatarLeft + dpToPx(20) : avatarRight, avatarBottom);
            }
        }
    }

    public void setCheck(boolean check) {
        if (haveCheckBox)
            checkBox.setChecked(check, true);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Theme.getInstance().getDividerColor(getContext()));
        canvas.drawLine(isRtl ? 4 : avatarImageView.getRight(), getMeasuredHeight() - 1, isRtl ? avatarImageView.getLeft() : getWidth(), getMeasuredHeight(), paint);
        super.dispatchDraw(canvas);
    }

    private void avatarHandler(RealmRoom item, AvatarHandler avatarHandler) {
        long idForGetAvatar;
        if (item.getType() == CHAT) {
            idForGetAvatar = item.getChatRoom().getPeerId();
        } else {
            idForGetAvatar = item.getId();
        }

        avatarHandler.getAvatar(new ParamWithInitBitmap(avatarImageView, idForGetAvatar)
                .initBitmap(HelperImageBackColor.drawAlphabetOnPicture((int)
                        getContext().getResources().getDimension(R.dimen.dp52), item.getInitials(), item.getColor())));
    }

    private void getLastMessage(RealmRoom room, AppCompatTextView lastMessageTv) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (room.getActionState() != null && room.getActionStateUserId() != AccountManager.getInstance().getCurrentUser().getId()) {

            SpannableString typingSpannableString = new SpannableString(room.getActionState());
            typingSpannableString.setSpan(new ForegroundColorSpan(Theme.getInstance().getAccentColor(lastMessageTv.getContext())), 0, room.getActionState().length(), 0);

            builder.append(typingSpannableString);

        } else if (room.getDraft() != null && !TextUtils.isEmpty(room.getDraft().getMessage())) {
            String draft = getResources().getString(R.string.txt_draft) + " ";

            SpannableString redSpannable = new SpannableString(draft);
            redSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(lastMessageTv.getContext(), R.color.red)), 0, draft.length(), 0);

            String draftMessage = room.getDraft().getMessage();
            SpannableString message = new SpannableString(draftMessage);
            message.setSpan(new ForegroundColorSpan(Theme.getInstance().getSendMessageTextColor(lastMessageTv.getContext())), 0, message.length(), 0);

            builder.append(redSpannable);
            builder.append(message);

        } else {
            boolean haveAttachment = false;
            boolean haveSenderName = false;
            boolean nameIsPersian = false;


            RealmRoomMessage lastMessage;
            if (room.getLastMessage() != null) {
                if (room.getLastMessage().getForwardMessage() != null) {
                    lastMessage = room.getLastMessage().getForwardMessage();
                } else {
                    lastMessage = room.getLastMessage();
                }

                if (lastMessage.isDeleted()) {
                    String deletedMessage = getResources().getString(R.string.deleted_message);
                    SpannableString deletedSpannable = new SpannableString(deletedMessage);
                    deletedSpannable.setSpan(new ForegroundColorSpan(Theme.getInstance().getSendMessageTextColor(lastMessageTv.getContext())), 0, deletedMessage.length(), 0);
                    builder.append(deletedSpannable);
                    lastMessageTv.setText(builder, TextView.BufferType.SPANNABLE);
                    return;
                }

                if (lastMessage.getMessage() != null) {
                    String attachmentTag = null;
                    String senderNameTag = null;
                    SpannableString attachmentSpannable = null;
                    SpannableString senderNameSpannable = null;
                    SpannableString lastMessageSpannable;
                    SpannableString senderNameQuoteSpannable = null;


                    if (room.getType() == GROUP && lastMessage.getMessageType() != ProtoGlobal.RoomMessageType.LOG) {
                        if (lastMessage.isAuthorMe() && room.getLastMessage().getForwardMessage() == null) {
                            senderNameTag = getResources().getString(R.string.txt_you);
                            senderNameSpannable = new SpannableString(senderNameTag);
                        } else {
                            if (room.getLastMessage().isAuthorMe()) {
                                senderNameTag = getResources().getString(R.string.txt_you);
                                senderNameSpannable = new SpannableString(senderNameTag);
                            } else {
                                try {
                                    RealmRegisteredInfo realmRegisteredInfo;
                                    if (room.getLastMessage().getForwardMessage() != null)
                                        realmRegisteredInfo = RealmRegisteredInfo.getRegistrationInfo(room.getRealm(), room.getLastMessage().getUserId());
                                    else
                                        realmRegisteredInfo = RealmRegisteredInfo.getRegistrationInfo(room.getRealm(), lastMessage.getUserId());

                                    if (realmRegisteredInfo != null && realmRegisteredInfo.getDisplayName() != null) {
                                        senderNameTag = realmRegisteredInfo.getDisplayName();
                                        senderNameSpannable = new SpannableString(senderNameTag);
                                        nameIsPersian = Character.getDirectionality(realmRegisteredInfo.getDisplayName().charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
                                    }
                                } catch (Exception e) {
                                    Log.e(getClass().getName(), "room list last message: ", e);
                                }
                            }
                        }

                        if (senderNameSpannable != null) {
                            haveSenderName = true;
                            senderNameSpannable.setSpan(new ForegroundColorSpan(Theme.getInstance().getAccentColor(lastMessageTv.getContext())), 0, senderNameTag.length(), 0);
                        }
                    }

                    switch (lastMessage.getMessageType()) {
                        case IMAGE_TEXT:
                            attachmentTag = AppUtils.getEmojiByUnicode(IMAGE);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case GIF_TEXT:
                            attachmentTag = AppUtils.getEmojiByUnicode(GIF);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case FILE_TEXT:
                            attachmentTag = AppUtils.getEmojiByUnicode(FILE);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case VIDEO_TEXT:
                            attachmentTag = AppUtils.getEmojiByUnicode(VIDEO);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case AUDIO_TEXT:
                            attachmentTag = AppUtils.getEmojiByUnicode(MUSIC);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case GIF:
                            attachmentTag = getResources().getString(R.string.gif_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case VOICE:
                            attachmentTag = getResources().getString(R.string.voice_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case LOG:
                            attachmentTag = HelperLogMessage.deserializeLog(lastMessageTv.getContext(), lastMessage.getLogs(), false).toString();
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case AUDIO:
                            attachmentTag = AppUtils.getEmojiByUnicode(MUSIC) + lastMessage.getAttachment().getName();
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case FILE:
                            attachmentTag = getResources().getString(R.string.file_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case IMAGE:
                            attachmentTag = getResources().getString(R.string.image_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case VIDEO:
                            attachmentTag = getResources().getString(R.string.video_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case WALLET:
                            builder.append(AppUtils.getEmojiByUnicode(WALLET));
                            if (lastMessage.getRoomMessageWallet() != null) {
                                String type = lastMessage.getRoomMessageWallet().getType();
                                if (type.equals(CARD_TO_CARD.toString())) {
                                    attachmentTag = getResources().getString(R.string.card_to_card_message);
                                } else if (type.equals(TOPUP.toString())) {
                                    attachmentTag = getResources().getString(R.string.topUp_message);
                                } else if (type.equals(BILL.toString())) {
                                    attachmentTag = getResources().getString(R.string.bill_message);
                                } else if (type.equals(PAYMENT.toString())) {
                                    attachmentTag = getResources().getString(R.string.payment_message);
                                } else if (type.equals(MONEY_TRANSFER.toString())) {
                                    attachmentTag = getResources().getString(R.string.wallet_message);
                                } else {
                                    attachmentTag = getResources().getString(R.string.unknown_message);
                                }
                            } else
                                attachmentTag = getResources().getString(R.string.wallet_message);

                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case CONTACT:
                            attachmentTag = getResources().getString(R.string.contact_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case STICKER:
                            attachmentTag = getResources().getString(R.string.sticker_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                        case LOCATION:
                            attachmentTag = getResources().getString(R.string.location_message);
                            attachmentSpannable = new SpannableString(attachmentTag);
                            break;
                    }

                    if (attachmentSpannable != null) {
                        haveAttachment = true;
                        attachmentSpannable.setSpan(new ForegroundColorSpan(Theme.getInstance().getAccentColor(lastMessageTv.getContext())), 0, attachmentTag.length(), 0);
                    }

                    if (haveSenderName) {
                        senderNameQuoteSpannable = new SpannableString(haveAttachment ? ":" : nameIsPersian ? ": " : ": ");
                        senderNameQuoteSpannable.setSpan(new ForegroundColorSpan(Theme.getInstance().getAccentColor(lastMessageTv.getContext())), 0, senderNameQuoteSpannable.length(), 0);
                    }
                    String message;
                    if (lastMessage.getMessage().length() > 70) {
                        message = lastMessage.getMessage().substring(0, 70) + "...";
                    } else
                        message = lastMessage.getMessage();

                    ArrayList<Tuple<Integer, Integer>> boldPlaces = AbstractMessage.getBoldPlaces(message);
                    message = AbstractMessage.removeBoldMark(message, boldPlaces);

//                    if (HelperCalander.isPersianUnicode)
//                        message = HelperCalander.convertToUnicodeFarsiNumber(message);

                    lastMessageSpannable = new SpannableString(/*subStringInternal(*/message/*)*/);
                    lastMessageSpannable.setSpan(new ForegroundColorSpan(Theme.getInstance().getSendMessageTextColor(lastMessageTv.getContext())), 0, lastMessageSpannable.length(), 0);

                    if (haveSenderName) {
                        if (haveAttachment) {
                            builder.append(senderNameSpannable).append(senderNameQuoteSpannable).append(attachmentSpannable).append(lastMessageSpannable);
                        } else
                            builder.append(senderNameSpannable).append(senderNameQuoteSpannable).append(lastMessageSpannable);
                    } else {
                        if (haveAttachment) {
                            builder.append(attachmentSpannable).append(lastMessageSpannable);
                        } else
                            builder.append(lastMessageSpannable);
                    }

                }
            }
        }
        lastMessageTv.setText(EmojiManager.getInstance().replaceEmoji(builder, lastMessageTv.getPaint().getFontMetricsInt(), -1, false), TextView.BufferType.SPANNABLE);
    }

    private int dpToPx(int dp) {
        return LayoutCreator.dpToPx(dp);
    }

    private int makeMeasureSpec(int size, int mode) {
        return LayoutCreator.manageSpec(size, mode);
    }

    private String getUnreadCount(int unreadCount) {
        if (unreadCount > 999) {
            if (isRtl)
                return HelperCalander.convertToUnicodeFarsiNumber("+999");
            else
                return "+999";
        } else {
            String s = String.valueOf(unreadCount);
            if (isRtl)
                return HelperCalander.convertToUnicodeFarsiNumber(s);
            else
                return s;

        }
    }

}
