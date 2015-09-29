package co.in.divi.llauncher;

import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

/**
 * Created by indra83 on 9/29/15.
 */
public class Util {
    	public static void fixBackgroundRepeat(View view) {
		Drawable bg = view.getBackground();
		if (bg != null) {
			if (bg instanceof LayerDrawable) {
				// Log.d("temp", "Hooray! setting bitmap tile mode");
				BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) bg).getDrawable(0);
				bd.mutate(); // make sure that we aren't sharing state anymore
				bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			} else if (bg instanceof StateListDrawable) {
				StateListDrawable stateDrwbl = (StateListDrawable) bg;
				stateDrwbl.mutate(); // make sure that we aren't sharing state
										// anymore

				Drawable.ConstantState constantState = stateDrwbl.getConstantState();
				if (constantState instanceof DrawableContainer.DrawableContainerState) {
					DrawableContainer.DrawableContainerState drwblContainerState = (DrawableContainer.DrawableContainerState) constantState;
					final Drawable[] drawables = drwblContainerState.getChildren();
					for (Drawable drwbl : drawables) {
						if (drwbl instanceof LayerDrawable) {
							BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) drwbl).getDrawable(0);
							// bd.mutate(); // make sure that we aren't sharing
							// state anymore
							bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
						}
					}
				}
			}
		}
	}
}
