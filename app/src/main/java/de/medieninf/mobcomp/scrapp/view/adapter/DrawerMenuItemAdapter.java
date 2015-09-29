package de.medieninf.mobcomp.scrapp.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.view.DrawerMenuItem;

/**
 * Adapter for the navigation drawer.
 */
public class DrawerMenuItemAdapter extends BaseAdapter {
    private List<DrawerMenuItem> items;
    private Context context;

    public DrawerMenuItemAdapter(Context context, List<DrawerMenuItem> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_menu_item, parent, false);
        }

        ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_menu_item_icon);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_menu_item_title);

        DrawerMenuItem item = items.get(position);
        ivIcon.setImageResource(item.getIcon());
        tvTitle.setText(item.getTitle());

        return convertView;
    }
}
