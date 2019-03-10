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

        String typeNames[] = {
                "bug",
                "dark",
                "dragon",
                "electric",
                "fairy",
                "fighting",
                "fire",
                "flying",
                "ghost",
                "grass",
                "ground",
                "ice",
                "normal",
                "poison",
                "psychic",
                "rock",
                "shadow",
                "steel",
                "unknown",
                "water",
        };

        int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        for (String name : typeNames) {
            ImageView im = new ImageView(container.getContext());
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(width, height);
            llp.setMargins(margin, margin, margin, 0);
            im.setLayoutParams(llp);
            im.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // TODO: extract this into a helper that loads an asset and returns a drawable
            try {
                InputStream stream = assets.open(String.format(Locale.US, "types/%s.png", name));
                Drawable drawable = Drawable.createFromStream(stream, name+".png");
                im.setImageDrawable(drawable);
            } catch (IOException exc) {
                im.setImageResource(R.drawable.ic_poke_unknown);
            }
            view.addView(im);
        }

        return view;
    }

}