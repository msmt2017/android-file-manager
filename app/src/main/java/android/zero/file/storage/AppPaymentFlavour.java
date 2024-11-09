package android.zero.file.storage;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.zero.R;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppPaymentFlavour extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static boolean isPurchased() {
		return true;
	}

	public void loadOwnedPurchasesFromGoogle() {
	}

	public void reloadSubscription() {
	}

	private void getPurchSkuDetails() {

	}

	public static String getPurchaseId(){
		return "";
	}

	public static String getPurchasedProductId(){
		return "";
	}

	private static boolean isProVersion() {
		return false;
	}

	public boolean isBillingSupported() {
		return false;
	}

	public void purchase(Activity activity, String productId){

	}

	public static void openPurchaseActivity(Context context){

	}
}