package com.kcrason.highperformancefriendscircle.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.kcrason.highperformancefriendscircle.Constants;
import com.kcrason.highperformancefriendscircle.enums.TranslationState;
import com.kcrason.highperformancefriendscircle.interfaces.OnItemClickPopupMenuListener;
import com.kcrason.highperformancefriendscircle.interfaces.OnPraiseOrCommentClickListener;
import com.kcrason.highperformancefriendscircle.span.TextMovementMethod;
import com.kcrason.highperformancefriendscircle.utils.TimerUtils;
import com.kcrason.highperformancefriendscircle.utils.Utils;
import com.kcrason.highperformancefriendscircle.widgets.CommentOrPraisePopupWindow;
import com.kcrason.highperformancefriendscircle.widgets.NineGridView;
import com.kcrason.highperformancefriendscircle.R;
import com.kcrason.highperformancefriendscircle.beans.FriendCircleBean;
import com.kcrason.highperformancefriendscircle.beans.OtherInfoBean;
import com.kcrason.highperformancefriendscircle.beans.UserBean;
import com.kcrason.highperformancefriendscircle.widgets.VerticalCommentWidget;
import java.util.ArrayList;
import java.util.List;
import ch.ielse.view.imagewatcher.ImageWatcher;

/**
 * 微信朋友圈列表适配器
 */
public class FriendCircleAdapter extends RecyclerView.Adapter<FriendCircleAdapter.BaseFriendCircleViewHolder>
        implements OnItemClickPopupMenuListener {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private List<FriendCircleBean> mFriendCircleBeans;

    private RequestOptions mRequestOptions;

    private int mAvatarSize;  // 头像显示的尺寸

    private DrawableTransitionOptions mDrawableTransitionOptions;

    private CommentOrPraisePopupWindow mCommentOrPraisePopupWindow;

    private OnPraiseOrCommentClickListener mOnPraiseOrCommentClickListener;

    private LinearLayoutManager mLayoutManager;

    private RecyclerView mRecyclerView;

    private ImageWatcher mImageWatcher;  // 微信朋友圈 图片显示各种骚操作的框架

    public FriendCircleAdapter(Context context, RecyclerView recyclerView, ImageWatcher imageWatcher) {
        this.mContext = context;
        this.mImageWatcher = imageWatcher;
        mRecyclerView = recyclerView;
        this.mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        this.mAvatarSize = Utils.dp2px(44f);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRequestOptions = new RequestOptions().centerCrop();
        this.mDrawableTransitionOptions = DrawableTransitionOptions.withCrossFade();
        if (context instanceof OnPraiseOrCommentClickListener) {
            this.mOnPraiseOrCommentClickListener = (OnPraiseOrCommentClickListener) context;
        }
    }

    // 给适配器设置数据并更新
    public void setFriendCircleBeans(List<FriendCircleBean> friendCircleBeans) {
        this.mFriendCircleBeans = friendCircleBeans;
        notifyDataSetChanged();
    }

    // 添加数据
    public void addFriendCircleBeans(List<FriendCircleBean> friendCircleBeans) {
        if (friendCircleBeans != null) {
            if (mFriendCircleBeans == null) {
                mFriendCircleBeans = new ArrayList<>();
            }
            this.mFriendCircleBeans.addAll(friendCircleBeans);
            notifyItemRangeInserted(mFriendCircleBeans.size(), friendCircleBeans.size());
        }
    }

    @Override
    public BaseFriendCircleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_ONLY_WORD) { // 纯文字布局
            return new OnlyWordViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_only_word, parent, false));
        } else if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_WORD_AND_URL) {  // 分享链接
            return new WordAndUrlViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_word_and_url, parent, false));
        } else if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_WORD_AND_IMAGES) {  // 文字和图片
            return new WordAndImagesViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_word_and_images, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseFriendCircleViewHolder holder, int position) {
        if (holder != null && mFriendCircleBeans != null && position < mFriendCircleBeans.size()) {
            FriendCircleBean friendCircleBean = mFriendCircleBeans.get(position);
            makeUserBaseData(holder, friendCircleBean, position);
            if (holder instanceof OnlyWordViewHolder) {  // 纯文字布局
                OnlyWordViewHolder onlyWordViewHolder = (OnlyWordViewHolder) holder;
            } else if (holder instanceof WordAndUrlViewHolder) {  // 分享链接
                WordAndUrlViewHolder wordAndUrlViewHolder = (WordAndUrlViewHolder) holder;
                // 分享链接的点击事件
                wordAndUrlViewHolder.layoutUrl.setOnClickListener(v -> Toast.makeText(mContext, "You Click Layout Url", Toast.LENGTH_SHORT).show());
            } else if (holder instanceof WordAndImagesViewHolder) {  // 文字和图片
                WordAndImagesViewHolder wordAndImagesViewHolder = (WordAndImagesViewHolder) holder;
                // 九宫格图片的点击事件
                wordAndImagesViewHolder.nineGridView.setOnImageClickListener((position1, view) ->
                    mImageWatcher.show((ImageView) view, wordAndImagesViewHolder.nineGridView.getImageViews(),
                            friendCircleBean.getImageUrls()));

                // 图片的适配器
                wordAndImagesViewHolder.nineGridView.setAdapter(new NineImageAdapter(mContext, mRequestOptions,
                        mDrawableTransitionOptions, friendCircleBean.getImageUrls()));
            }
        }
    }


    private void makeUserBaseData(BaseFriendCircleViewHolder holder, FriendCircleBean friendCircleBean, int position) {
        holder.txtContent.setText(friendCircleBean.getContentSpan());
        setContentShowState(holder, friendCircleBean);
        // 朋友圈文字内容的长按事件
        holder.txtContent.setOnLongClickListener(v -> {
            TranslationState translationState = friendCircleBean.getTranslationState();
            if (translationState == TranslationState.END) {
                Utils.showPopupMenu(mContext, this, position, v, TranslationState.END);
            } else {
                Utils.showPopupMenu(mContext, this, position, v, TranslationState.START);
            }
            return true;
        });

        // 翻译布局更新
        updateTargetItemContent(position, holder, friendCircleBean.getTranslationState(),
                friendCircleBean.getContentSpan(), false);

        // 用户信息更新
        UserBean userBean = friendCircleBean.getUserBean();
        if (userBean != null) {
            holder.txtUserName.setText(userBean.getUserName());
            Glide.with(mContext).load(userBean.getUserAvatarUrl())
                    .apply(mRequestOptions.override(mAvatarSize, mAvatarSize))
                    .transition(mDrawableTransitionOptions)
                    .into(holder.imgAvatar);
        }

        OtherInfoBean otherInfoBean = friendCircleBean.getOtherInfoBean();

        if (otherInfoBean != null) {
            holder.txtSource.setText(otherInfoBean.getSource());  // 信息来源
            holder.txtPublishTime.setText(otherInfoBean.getTime());  // 发布时间
        }

        if (friendCircleBean.isShowPraise() || friendCircleBean.isShowComment()) {
            holder.layoutPraiseAndComment.setVisibility(View.VISIBLE); // 点赞和评论的整体布局
            if (friendCircleBean.isShowComment() && friendCircleBean.isShowPraise()) {
                holder.viewLine.setVisibility(View.VISIBLE);
            } else {
                holder.viewLine.setVisibility(View.GONE);
            }
            if (friendCircleBean.isShowPraise()) {
                holder.txtPraiseContent.setVisibility(View.VISIBLE); // 点赞的人名
                holder.txtPraiseContent.setText(friendCircleBean.getPraiseSpan());
            } else {
                holder.txtPraiseContent.setVisibility(View.GONE);
            }
            if (friendCircleBean.isShowComment()) {
                holder.verticalCommentWidget.setVisibility(View.VISIBLE);  // 评论布局
                holder.verticalCommentWidget.addComments(friendCircleBean.getCommentBeans(), false);
            } else {
                holder.verticalCommentWidget.setVisibility(View.GONE);
            }
        } else {
            holder.layoutPraiseAndComment.setVisibility(View.GONE);
        }

        // 点击显示 点赞和评论的 弹框
        holder.imgPraiseOrComment.setOnClickListener(v -> {
            if (mContext instanceof Activity) {
                if (mCommentOrPraisePopupWindow == null) {
                    mCommentOrPraisePopupWindow = new CommentOrPraisePopupWindow(mContext);
                }
                mCommentOrPraisePopupWindow
                        .setOnPraiseOrCommentClickListener(mOnPraiseOrCommentClickListener)
                        .setCurrentPosition(position);
                if (mCommentOrPraisePopupWindow.isShowing()) {
                    mCommentOrPraisePopupWindow.dismiss();
                } else {
                    mCommentOrPraisePopupWindow.showPopupWindow(v);
                }
            }
        });

        holder.txtLocation.setOnClickListener(v -> Toast.makeText(mContext, "You Click Location", Toast.LENGTH_SHORT).show());
    }

    // 朋友圈发布文字现实的状态 全文还是收起
    private void setContentShowState(BaseFriendCircleViewHolder holder, FriendCircleBean friendCircleBean) {
        if (friendCircleBean.isShowCheckAll()) {
            holder.txtState.setVisibility(View.VISIBLE);
            setTextState(holder, friendCircleBean.isExpanded());
            holder.txtState.setOnClickListener(v -> {
                if (friendCircleBean.isExpanded()) {
                    friendCircleBean.setExpanded(false);
                } else {
                    friendCircleBean.setExpanded(true);
                }
                setTextState(holder, friendCircleBean.isExpanded());
            });
        } else {
            holder.txtState.setVisibility(View.GONE);
            holder.txtContent.setMaxLines(Integer.MAX_VALUE);
        }
    }

    private void setTextState(BaseFriendCircleViewHolder holder, boolean isExpand) {
        if (isExpand) {
            holder.txtContent.setMaxLines(Integer.MAX_VALUE);
            holder.txtState.setText("收起");
        } else {
            holder.txtContent.setMaxLines(4);
            holder.txtState.setText("全文");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mFriendCircleBeans.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mFriendCircleBeans == null ? 0 : mFriendCircleBeans.size();
    }

    // 复制的点击事件
    @Override
    public void onItemClickCopy(int position) {
        Toast.makeText(mContext, "已复制", Toast.LENGTH_SHORT).show();
    }

    // 点击选择翻译
    @Override
    public void onItemClickTranslation(int position) {
        if (mFriendCircleBeans != null && position < mFriendCircleBeans.size()) {
            mFriendCircleBeans.get(position).setTranslationState(TranslationState.CENTER);
            notifyTargetItemView(position, TranslationState.CENTER, null);
            TimerUtils.timerTranslation(() -> {
                if (mFriendCircleBeans != null && position < mFriendCircleBeans.size()) {
                    mFriendCircleBeans.get(position).setTranslationState(TranslationState.END);
                    notifyTargetItemView(position, TranslationState.END, mFriendCircleBeans.get(position).getContentSpan());
                }
            });
        }
    }

    // 点击隐藏翻译
    @Override
    public void onItemClickHideTranslation(int position) {
        if (mFriendCircleBeans != null && position < mFriendCircleBeans.size()) {
            mFriendCircleBeans.get(position).setTranslationState(TranslationState.START);
            notifyTargetItemView(position, TranslationState.START, null);
        }
    }

    // 翻译内容
    private void updateTargetItemContent(int position, BaseFriendCircleViewHolder baseFriendCircleViewHolder,
                                         TranslationState translationState, SpannableStringBuilder translationResult, boolean isStartAnimation) {
        if (translationState == TranslationState.START) {
            baseFriendCircleViewHolder.layoutTranslation.setVisibility(View.GONE);
        } else if (translationState == TranslationState.CENTER) {
            baseFriendCircleViewHolder.layoutTranslation.setVisibility(View.VISIBLE);
            baseFriendCircleViewHolder.divideLine.setVisibility(View.GONE);
            baseFriendCircleViewHolder.translationTag.setVisibility(View.VISIBLE);
            baseFriendCircleViewHolder.translationDesc.setText(R.string.translating);
            baseFriendCircleViewHolder.txtTranslationContent.setVisibility(View.GONE);
            Utils.startAlphaAnimation(baseFriendCircleViewHolder.translationDesc, isStartAnimation);
        } else {
            baseFriendCircleViewHolder.layoutTranslation.setVisibility(View.VISIBLE);
            baseFriendCircleViewHolder.divideLine.setVisibility(View.VISIBLE);
            baseFriendCircleViewHolder.translationTag.setVisibility(View.GONE);
            baseFriendCircleViewHolder.translationDesc.setText(R.string.translated);
            baseFriendCircleViewHolder.txtTranslationContent.setVisibility(View.VISIBLE);
            baseFriendCircleViewHolder.txtTranslationContent.setText(translationResult);
            Utils.startAlphaAnimation(baseFriendCircleViewHolder.txtTranslationContent, isStartAnimation);
            baseFriendCircleViewHolder.txtTranslationContent.setOnLongClickListener(v -> {
                Utils.showPopupMenu(mContext, FriendCircleAdapter.this, position, v, TranslationState.END);
                return true;
            });
        }
    }

    // 更新翻译内容
    private void notifyTargetItemView(int position, TranslationState translationState, SpannableStringBuilder translationResult) {
        View childView = mLayoutManager.findViewByPosition(position);
        if (childView != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(childView);
            if (viewHolder instanceof BaseFriendCircleViewHolder) {
                BaseFriendCircleViewHolder baseFriendCircleViewHolder = (BaseFriendCircleViewHolder) viewHolder;
                updateTargetItemContent(position, baseFriendCircleViewHolder,
                        translationState, translationResult, true);
            }
        }
    }

    @Override
    public void onItemClickCollection(int position) {
        Toast.makeText(mContext, "已收藏", Toast.LENGTH_SHORT).show();
    }

    static class WordAndImagesViewHolder extends BaseFriendCircleViewHolder {

        NineGridView nineGridView;  // 九宫图布局

        public WordAndImagesViewHolder(View itemView) {
            super(itemView);
            nineGridView = itemView.findViewById(R.id.nine_grid_view);
        }
    }


    static class WordAndUrlViewHolder extends BaseFriendCircleViewHolder {

        LinearLayout layoutUrl;  // 分享的链接佈局

        public WordAndUrlViewHolder(View itemView) {
            super(itemView);
            layoutUrl = itemView.findViewById(R.id.layout_url);
        }
    }

    static class OnlyWordViewHolder extends BaseFriendCircleViewHolder {

        public OnlyWordViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class BaseFriendCircleViewHolder extends RecyclerView.ViewHolder {

        public VerticalCommentWidget verticalCommentWidget;  // 评论布局
        public TextView txtUserName;  // 用户昵称
        public View viewLine;  // 点赞与评论之间的分割线
        public TextView txtPraiseContent;   // 点赞的人名
        public ImageView imgAvatar;  // 头像
        public TextView txtSource;    // 来源 QQ 、QQ空间等等
        public TextView txtPublishTime;  // 几天前
        public ImageView imgPraiseOrComment;   // 点击显示 点赞和评论的 弹框
        public TextView txtLocation;  // 定位的地址
        public TextView txtContent;  // 发布在朋友圈的内容
        public TextView txtState;    // 全文 点击展开
        public LinearLayout layoutTranslation;  // 翻译的整体布局
        public TextView txtTranslationContent;  // 翻译后的内容控件
        public View divideLine;   // 翻译和翻译后内容的分割线
        public ImageView translationTag;  // 翻译时间的图片标记
        public TextView translationDesc;  // 翻译中、已翻译的控件
        public LinearLayout layoutPraiseAndComment;  // 点赞和评论的整体布局

        public BaseFriendCircleViewHolder(View itemView) {
            super(itemView);
            verticalCommentWidget = itemView.findViewById(R.id.vertical_comment_widget);
            txtUserName = itemView.findViewById(R.id.txt_user_name);
            txtPraiseContent = itemView.findViewById(R.id.praise_content);
            viewLine = itemView.findViewById(R.id.view_line);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtSource = itemView.findViewById(R.id.txt_source);
            txtPublishTime = itemView.findViewById(R.id.txt_publish_time);
            imgPraiseOrComment = itemView.findViewById(R.id.img_click_praise_or_comment);
            txtLocation = itemView.findViewById(R.id.txt_location);
            txtContent = itemView.findViewById(R.id.txt_content);
            txtState = itemView.findViewById(R.id.txt_state);
            txtTranslationContent = itemView.findViewById(R.id.txt_translation_content);
            layoutTranslation = itemView.findViewById(R.id.layout_translation);
            layoutPraiseAndComment = itemView.findViewById(R.id.layout_praise_and_comment);
            divideLine = itemView.findViewById(R.id.view_divide_line);
            translationTag = itemView.findViewById(R.id.img_translating);
            translationDesc = itemView.findViewById(R.id.txt_translation_desc);
            txtPraiseContent.setMovementMethod(new TextMovementMethod());
        }
    }
}
