package rec.games.pokemon.teambuilder;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class TypeReportFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.type_report, container, false);

        AssetManager assets = container.getContext().getAssets();

        ImageView ivFlying = view.findViewById(R.id.iv_type_flying);
        try {
            InputStream stream = assets.open("types/flying.png");
            Drawable drawable = Drawable.createFromStream(stream, "flying.png");
            ivFlying.setImageDrawable(drawable);
        } catch (IOException exc) {
            ivFlying.setImageResource(R.drawable.ic_poke_unknown);
        }

        return view;
    }

}