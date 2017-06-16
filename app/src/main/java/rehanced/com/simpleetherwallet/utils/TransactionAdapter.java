package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context context;
    private List<TransactionDisplay> boxlist;
    private int lastPosition = -1;
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm");
    private View.OnCreateContextMenuListener contextMenuListener;
    private View.OnClickListener clickListener;
    private int position;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView  month, walletbalance, walletname, other_address, plusminus;
        public ImageView my_addressicon, other_addressicon, type;
        private LinearLayout container;

        public MyViewHolder(View view) {
            super(view);
            month = (TextView) view.findViewById(R.id.month);
            walletbalance = (TextView) view.findViewById(R.id.walletbalance);
            plusminus = (TextView) view.findViewById(R.id.plusminus);
            walletname = (TextView) view.findViewById(R.id.walletname);
            other_address = (TextView) view.findViewById(R.id.other_address);

            my_addressicon= (ImageView) view.findViewById(R.id.my_addressicon);
            other_addressicon= (ImageView) view.findViewById(R.id.other_addressicon);
            type = (ImageView) view.findViewById(R.id.type);
            container = (LinearLayout) view.findViewById(R.id.container);
        }

        public void clearAnimation() {
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
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_w_transaction, parent, false);
        itemView.setOnCreateContextMenuListener(contextMenuListener);
        itemView.setOnClickListener(clickListener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        TransactionDisplay box = boxlist.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(position);
                return false;
            }
        });

        holder.walletbalance.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(Math.abs(box.getAmount()), ExchangeCalculator.getInstance().getCurrent().getRate()))+" "+ ExchangeCalculator.getInstance().getCurrencyShort());

        String walletname = AddressNameConverter.getInstance(context).get(box.getFromAddress());
        holder.walletname.setText(walletname == null ? box.getWalletName() : walletname);

        String toName = AddressNameConverter.getInstance(context).get(box.getToAddress());
        holder.other_address.setText(toName == null ? box.getToAddress() : toName + " ("+box.getToAddress().substring(0, 10) +")");
        holder.plusminus.setText(box.getAmount() > 0 ? "+" : "-");

        holder.plusminus.setTextColor(context.getResources().getColor(box.getAmount() > 0 ? R.color.etherReceived : R.color.etherSpent));
        holder.walletbalance.setTextColor(context.getResources().getColor(box.getAmount() > 0 ? R.color.etherReceived : R.color.etherSpent));
        holder.container.setAlpha(1f);
        if(box.getConfirmationStatus() == 0) {
            holder.month.setText("Unconfirmed");
            holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmedNew));
            holder.container.setAlpha(0.75f);
        }else if(box.getConfirmationStatus() > 12) {
            holder.month.setText(dateformat.format(new Date(box.getDate())));
            holder.month.setTextColor(context.getResources().getColor(R.color.normalBlack));
        }else {
            holder.month.setText(box.getConfirmationStatus() + " / 12 Confirmations");
            holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmed));
        }

        holder.type.setVisibility(box.getType() == TransactionDisplay.NORMAL ? View.INVISIBLE : View.VISIBLE);
        holder.my_addressicon.setImageBitmap(Blockies.createIcon(box.getFromAddress().toLowerCase()));
        holder.other_addressicon.setImageBitmap(Blockies.createIcon(box.getToAddress().toLowerCase()));

        setAnimation(holder.container, position);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
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
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return boxlist.size();
    }
}
