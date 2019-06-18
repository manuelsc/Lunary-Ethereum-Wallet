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

import java.util.List;

import me.grantland.widget.AutofitTextView;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.data.WalletDisplay;


public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder> {

    private Context context;
    private List<WalletDisplay> boxlist;
    private int lastPosition = -1;
    private View.OnClickListener listener;
    private View.OnCreateContextMenuListener contextMenuListener;
    private int position;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView walletname, walletbalance;
        public ImageView addressimage, type;
        AutofitTextView walletaddress;
        public LinearLayout container;

        public MyViewHolder(View view) {
            super(view);
            walletaddress = (AutofitTextView) view.findViewById(R.id.walletaddress);
            walletname = (TextView) view.findViewById(R.id.walletname);
            walletbalance = (TextView) view.findViewById(R.id.walletbalance);
            addressimage = (ImageView) view.findViewById(R.id.addressimage);
            type = (ImageView) view.findViewById(R.id.type);
            container = (LinearLayout) view.findViewById(R.id.container);
        }

        public void clearAnimation() {
            container.clearAnimation();
        }
    }


    public WalletAdapter(List<WalletDisplay> boxlist, Context context, View.OnClickListener listener, View.OnCreateContextMenuListener l) {
        this.boxlist = boxlist;
        this.context = context;
        this.listener = listener;
        this.contextMenuListener = l;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_w_address, parent, false);

        itemView.setOnClickListener(listener);
        itemView.setOnCreateContextMenuListener(contextMenuListener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        WalletDisplay box = boxlist.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(position);
                return false;
            }
        });
        holder.walletaddress.setText(box.getPublicKey());
        String walletname = AddressNameConverter.getInstance(context).get(box.getPublicKey());
        holder.walletname.setText(walletname == null ? "New Wallet" : walletname);
        if (box.getType() != WalletDisplay.CONTACT && box.getBalance() >= 0)
            holder.walletbalance.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(box.getBalance(), ExchangeCalculator.getInstance().getCurrent().getRate())) + " " + ExchangeCalculator.getInstance().getCurrencyShort());
        holder.addressimage.setImageBitmap(Blockies.createIcon(box.getPublicKey()));

        holder.type.setVisibility(box.getType() == TransactionDisplay.NORMAL || box.getType() == WalletDisplay.CONTACT ? View.INVISIBLE : View.VISIBLE);

        setAnimation(holder.container, position);
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(WalletAdapter.MyViewHolder holder) {
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
