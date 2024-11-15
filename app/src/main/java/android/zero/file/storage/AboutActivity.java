/*
 * Copyright (C) 2014 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.zero.file.storage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import android.zero.file.storage.misc.AnalyticsManager;
import android.zero.file.storage.misc.ColorUtils;
import android.zero.file.storage.misc.SystemBarTintManager;
import android.zero.file.storage.misc.Utils;
import android.zero.file.storage.setting.SettingsActivity;

import static android.zero.file.storage.DocumentsActivity.getStatusBarHeight;
import static android.zero.file.storage.DocumentsApplication.isTelevision;
import static android.zero.file.storage.misc.Utils.getSuffix;
import static android.zero.file.storage.misc.Utils.openFeedback;
import static android.zero.file.storage.misc.Utils.openPlaystore;

import android.zero.R;
import android.zero.BuildConfig;

public class AboutActivity extends AboutVariantFlavour implements View.OnClickListener {

	public static final String TAG = "About";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Utils.hasKitKat() && !Utils.hasLollipop()){
			setTheme(R.style.DocumentsTheme_Translucent);
		}
		setContentView(R.layout.activity_about);

		int color = SettingsActivity.getPrimaryColor();
		View view = findViewById(R.id.toolbar);
		if(view instanceof Toolbar){
			Toolbar mToolbar = (Toolbar) view;
			mToolbar.setTitleTextAppearance(this, R.style.zeroTitleTextAppearance);
			if(Utils.hasKitKat() && !Utils.hasLollipop()) {
				mToolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
			}
			mToolbar.setBackgroundColor(color);
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(null);
			setUpDefaultStatusBar();
		} else {
			view.setBackgroundColor(color);
		}
		initAd();
		initControls();
	}

	@Override
	public String getTag() {
		return TAG;
	}

	private void initControls() {

		int accentColor = ColorUtils.getTextColorForBackground(SettingsActivity.getPrimaryColor());
		TextView logo = (TextView)findViewById(R.id.logo);
		logo.setTextColor(accentColor);
		String header = logo.getText() + getSuffix() + " v" + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " Debug" : "");
		logo.setText(header);

		TextView action_rate = (TextView)findViewById(R.id.action_rate);
		TextView action_support = (TextView)findViewById(R.id.action_support);
		TextView action_share = (TextView)findViewById(R.id.action_share);
		TextView action_feedback = (TextView)findViewById(R.id.action_feedback);
		TextView action_sponsor = (TextView)findViewById(R.id.action_sponsor);

		action_rate.setOnClickListener(this);
		action_support.setOnClickListener(this);
		action_share.setOnClickListener(this);
		action_feedback.setOnClickListener(this);
		action_sponsor.setOnClickListener(this);

		if(Utils.isOtherBuild()){
			action_rate.setVisibility(View.GONE);
			action_support.setVisibility(View.GONE);
		} else if(isTelevision()){
			action_share.setVisibility(View.GONE);
			action_feedback.setVisibility(View.GONE);
		}

		if(!DocumentsApplication.isPurchased()){
			action_sponsor.setVisibility(View.VISIBLE);
		}
	}

    @Override
    public void startActivity(Intent intent) {
        if(Utils.isIntentAvailable(this, intent)) {
            super.startActivity(intent);
        }
    }

	@Override
	public void onClick(View view) {
        
        int getview =view.getId();
        
        if(getview==R.id.action_feedback){
            openFeedback(this);
        } else if(getview==R.id.action_rate){
            openPlaystore(this);
				AnalyticsManager.logEvent("app_rate");
        } else if (getview==R.id.action_sponsor){
            showAd();
				AnalyticsManager.logEvent("app_sponsor");
        } else if (getview == R.id.action_support){
            if(Utils.isProVersion()){
					Intent intentMarketAll = new Intent("android.intent.action.VIEW");
					intentMarketAll.setData(Utils.getAppProStoreUri());
					startActivity(intentMarketAll);
				} else {
					DocumentsApplication.openPurchaseActivity(this);
				}
				AnalyticsManager.logEvent("app_love");
            
        } else if(getview == R.id.action_support){
            String shareText = "I found this file mananger very useful. Give it a try. "
						+ Utils.getAppShareUri().toString();
				ShareCompat.IntentBuilder
						.from(this)
						.setText(shareText)
						.setType("text/plain")
						.setChooserTitle("Share AnExplorer")
						.startChooser();
				AnalyticsManager.logEvent("app_share");
        }
        
        
        
		
	}

	public void setUpDefaultStatusBar() {
		int color = Utils.getStatusBarColor(SettingsActivity.getPrimaryColor());
		if(Utils.hasLollipop()){
			getWindow().setStatusBarColor(color);
		}
		else if(Utils.hasKitKat()){
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
			systemBarTintManager.setTintColor(Utils.getStatusBarColor(color));
			systemBarTintManager.setStatusBarTintEnabled(true);
		}
	}
}