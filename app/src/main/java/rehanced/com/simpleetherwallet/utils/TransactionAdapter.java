package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;


public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<TransactionDisplay> boxlist;
    private int lastPosition = -1;
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault());
    private View.OnCreateContextMenuListener contextMenuListener;
    private View.OnClickListener clickListener;
    private int position;

    private static final int CONTENT = 0;
    private static final int AD = 1;
    private static final int LIST_AD_DELTA = 9;

    @Override
    public int getItemViewType(int position) {
        if(!Settings.displayAds) return CONTENT;
        if (position % LIST_AD_DELTA == 0) {
            return AD;
        }
        return CONTENT;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView month, walletbalance, walletname, other_address, plusminus;
        ImageView my_addressicon, other_addressicon, type, error;
        private LinearLayout container;

        MyViewHolder(View view) {
            super(view);
            month = (TextView) view.findViewById(R.id.month);
            walletbalance = (TextView) view.findViewById(R.id.walletbalance);
            plusminus = (TextView) view.findViewById(R.id.plusminus);
            walletname = (TextView) view.findViewById(R.id.walletname);
            other_address = (TextView) view.findViewById(R.id.other_address);

            my_addressicon = (ImageView) view.findViewById(R.id.my_addressicon);
            other_addressicon = (ImageView) view.findViewById(R.id.other_addressicon);
            type = (ImageView) view.findViewById(R.id.type);
            error = (ImageView) view.findViewById(R.id.error);
            container = (LinearLayout) view.findViewById(R.id.container);
        }

        void clearAnimation() {
            container.clearAnimation();
        }
    }


    public TransactionAdapter(List<TransactionDisplay> boxlist, Context context, View.OnClickListener clickListener, View.OnCreateContextMenuListener listener) {
        this.boxlist = boxlist;
        this.context = context;
        this.contextMenuListener = listener;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CONTENT) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_w_transaction, parent, false);
            itemView.setOnCreateContextMenuListener(contextMenuListener);
            itemView.setOnClickListener(clickListener);
            return new MyViewHolder(itemView);
        } else {
            return new AdRecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_w_transaction_ad, parent, false));
        }
    }

    public static int calculateBoxPosition(int position){
        if(!Settings.displayAds) return position;
        if(position < LIST_AD_DELTA) return position - 1;
        return position - (position / LIST_AD_DELTA) - 1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder_, final int position) {
        if (getItemViewType(position) == CONTENT) {
            MyViewHolder holder = (MyViewHolder) holder_;
            TransactionDisplay box = boxlist.get(calculateBoxPosition(position));

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setPosition(position);
                    return false;
                }
            });

            holder.walletbalance.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(Math.abs(box.getAmount()), ExchangeCalculator.getInstance().getCurrent().getRate())) + " " + ExchangeCalculator.getInstance().getCurrencyShort());

            String walletname = AddressNameConverter.getInstance(context).get(box.getFromAddress());
            holder.walletname.setText(walletname == null ? box.getWalletName() : walletname);

            String toName = AddressNameConverter.getInstance(context).get(box.getToAddress());
            holder.other_address.setText(toName == null ? box.getToAddress() : toName + " (" + box.getToAddress().substring(0, 10) + ")");
            holder.plusminus.setText(box.getAmount() > 0 ? "+" : "-");

            holder.plusminus.setTextColor(context.getResources().getColor(box.getAmount() > 0 ? R.color.etherReceived : R.color.etherSpent));
            holder.walletbalance.setTextColor(context.getResources().getColor(box.getAmount() > 0 ? R.color.etherReceived : R.color.etherSpent));
            holder.container.setAlpha(1f);
            if (box.getConfirmationStatus() == 0) {
                holder.month.setText("Unconfirmed");
                holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmedNew));
                holder.container.setAlpha(0.75f);
            } else if (box.getConfirmationStatus() > 12) {
                holder.month.setText(dateformat.format(new Date(box.getDate())));
                holder.month.setTextColor(context.getResources().getColor(R.color.normalBlack));
            } else {
                holder.month.setText(box.getConfirmationStatus() + " / 12 Confirmations");
                holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmed));
            }

            holder.type.setVisibility(box.getType() == TransactionDisplay.NORMAL ? View.INVISIBLE : View.VISIBLE);
            holder.error.setVisibility(box.isError() ? View.VISIBLE : View.GONE);
            holder.my_addressicon.setImageBitmap(Blockies.createIcon(box.getFromAddress().toLowerCase()));
            holder.other_addressicon.setImageBitmap(Blockies.createIcon(box.getToAddress().toLowerCase()));

            setAnimation(holder.container, position);
        } else {
            AdRecyclerHolder holder = (AdRecyclerHolder) holder_;
            holder.loadAd(context);
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if(holder instanceof MyViewHolder)
            holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }


    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof MyViewHolder)
            ((MyViewHolder)holder).clearAnimation();
    }

    @Override
    public int getItemCount() {
        if(!Settings.displayAds) return boxlist.size();
        int additionalContent = 1;
        if (boxlist.size() > 0 && boxlist.size() >= LIST_AD_DELTA) {
            additionalContent += (boxlist.size() / (LIST_AD_DELTA-1));
        }
        return boxlist.size() + additionalContent;
    }
}