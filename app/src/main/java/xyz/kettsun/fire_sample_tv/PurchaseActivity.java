package xyz.kettsun.fire_sample_tv;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import xyz.kettsun.fire_sample_tv.model.AbebeSku;

public class PurchaseActivity extends Activity {
  private static final String TAG = PurchaseActivity.class.getSimpleName();

  private RequestId requestId;

  private static final String SKU = "xyz.kettsun.premium.plan";
  private static final String availableMarkpetplace = "JP";

  private UserIapData userIapData;


  private PurchasingListener listener = new PurchasingListener() {
    @Override public void onUserDataResponse(UserDataResponse response) {
      Log.d(TAG,
          "[IAP] onGetUserDataResponse: requestId (" + response.getRequestId() + ") userIdRequestStatus: "
              + response.getRequestStatus() + ")");

      final UserDataResponse.RequestStatus status = response.getRequestStatus();
      switch (status) {
        case SUCCESSFUL:
          Log.d(TAG, "[IAP] onUserDataResponse: get user id (" + response.getUserData().getUserId()
              + ", marketplace (" + response.getUserData().getMarketplace() + ") ");

          if (userIapData == null) {
            userIapData = new UserIapData(response.getUserData().getUserId(), response.getUserData().getMarketplace());
          }

          break;

        case FAILED:
        case NOT_SUPPORTED:
          Log.d(TAG, "[IAP] onUserDataResponse failed, status code is " + status);
          break;
      }
    }

    @Override public void onProductDataResponse(ProductDataResponse response) {
      Log.d(TAG, "[IAP] onProductDataResponse");
      final ProductDataResponse.RequestStatus status = response.getRequestStatus();
      switch (status) {
        case SUCCESSFUL:  // 成功
          /** 利用不能なプロダクト **/
          for (final String s : response.getUnavailableSkus()) {
            Log.v(TAG, "[IAP] Unavailable SKU:" + s);
          }
          /** 利用可能なプロダクト情報 **/
          final Map<String, Product> products = response.getProductData();
          for (final String key : products.keySet()) {
            Product product = products.get(key);
            Log.d(TAG, "[IAP] Title: " + product.getTitle());
            Log.d(TAG, "[IAP] ProductType: " + product.getProductType());
            Log.d(TAG, "[IAP] SKU: " + product.getSku());
            Log.d(TAG, "[IAP] Price: " + product.getPrice());
            Log.d(TAG, "[IAP] Description: " + product.getDescription());
          }
          break;

        case FAILED:
        case NOT_SUPPORTED:
          break;
      }
    }

    @Override public void onPurchaseResponse(PurchaseResponse response) {
      Log.d(TAG, "[IAP] onPurchaseResponse");
      final PurchaseResponse.RequestStatus status = response.getRequestStatus();
      switch (status) {
        case SUCCESSFUL:
          /** 購入履歴を参照 **/
          final Receipt receipt = response.getReceipt();
          final String receiptId = receipt.getReceiptId();
          final String sku = receipt.getSku();
          final ProductType productType = receipt.getProductType();
          final Date purchaseDate = receipt.getPurchaseDate();
          final Date cancelDate = receipt.getCancelDate();

          Log.d(TAG, "[IAP] ReceiptId: " + receiptId);
          Log.d(TAG, "[IAP] SKU: " + sku);
          Log.d(TAG, "[IAP] ProductType: " + productType.toString());
          Log.d(TAG, "[IAP] PurchaseDate: " + purchaseDate);
          Log.d(TAG, "[IAP] CancelDate: " + cancelDate);

          handleReceipt(response.getRequestId().toString(), receipt, response.getUserData());

          break;
        case ALREADY_PURCHASED:
          break;
        case FAILED:
          Log.d(TAG, "[IAP] purchase FAILED ");
          break;
      }
    }

    @Override public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
      Log.d(TAG, "---------- [IAP] onPurchaseUpdatesResponse ----------");
      switch (response.getRequestStatus()) {
        case SUCCESSFUL:
          Log.d(TAG, "[IAP] SUCCESS");
          userIapData = new UserIapData(response.getUserData().getUserId(), response.getUserData().getMarketplace());
          for ( final Receipt receipt : response.getReceipts()) {
            // Process receipts
            Log.d(TAG, "[IAP] ReceiptId: " + receipt.getReceiptId());
            Log.d(TAG, "[IAP] SKU: " + receipt.getSku());
            Log.d(TAG, "[IAP] ProductType: " + receipt.getProductType().toString());
            Log.d(TAG, "[IAP] PurchaseDate: " + receipt.getPurchaseDate().toString());
            if (receipt.getCancelDate() != null)Log.d(TAG, "[IAP] CancelDate: " + receipt.getCancelDate().toString());
            handleReceipt(response.getRequestId().toString(), receipt, response.getUserData());
          }

          if (response.hasMore()) {
            PurchasingService.getPurchaseUpdates(false);
          }
          break ;
        case FAILED:
        case NOT_SUPPORTED:
          Log.w(TAG, "********** [IAP] FAILED!! **********");
          break;
        default:
          Log.e(TAG, "********** [IAP] No Implemented status!! **********");
          break;
      }
    }


  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_purchase);
    PurchasingService.registerListener(this, listener);
  }

  /**
   * Call {@link PurchasingService#getProductData(Set)} to get the product
   * availability
   */
  @Override protected void onStart() {
    super.onStart();
    Log.d(TAG, "---------- [Lifecycle] onStart ----------");
    //Log.d(TAG, "[IAP] onStart: call getProductData for skus: " + Arrays.toString(AbebeSku.values()));
    //final Set<String> productSkus = new HashSet<String>();
    //for (final AbebeSku abbSku : AbebeSku.values()) {
    //  productSkus.add(abbSku.getSku());
    //}
    //PurchasingService.getProductData(productSkus);
  }

  @Override protected void onResume() {
    super.onResume();
    Log.d(TAG, "---------- [Lifecycle] onResume ----------");
    //PurchasingService.getUserData();
    //PurchasingService.getPurchaseUpdates(false);


    findViewById(R.id.get_product_data_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "---------- [IAP] getProductData ----------");
        final Set<String> productSkus = new HashSet<String>();
        for (final AbebeSku abbSku : AbebeSku.values()) {
          productSkus.add(abbSku.getSku());
        }
        PurchasingService.getProductData(productSkus);
      }
    });

    findViewById(R.id.get_user_data_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "---------- [IAP] getUserData ----------");
        PurchasingService.getUserData();
      }
    });

    findViewById(R.id.get_purchase_update_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "---------- [IAP] getPurchaseUpdates(false) ----------");
        PurchasingService.getPurchaseUpdates(false);
      }
    });

    findViewById(R.id.get_purchase_update_button_true).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "---------- [IAP] getPurchaseUpdates(true) ----------");
        PurchasingService.getPurchaseUpdates(true);
      }
    });

    findViewById(R.id.premium_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "---------- [IAP] purchase ----------");
        for (final AbebeSku abbSku : AbebeSku.values()) {
          requestId = PurchasingService.purchase(abbSku.getSku());
        }
      }
    });
  }


  /**
   * Show message on UI
   */
  public void showMessage(final String message) {
    Toast.makeText(PurchaseActivity.this, message, Toast.LENGTH_LONG).show();
  }

  /**
   * Method to handle receipt
   *
   * @param requestId
   * @param receipt
   * @param userData
   */
  public void handleReceipt(final String requestId, final Receipt receipt, final UserData userData) {
    Log.d(TAG, "---------- [IAP] handleReceipt ----------");
    switch (receipt.getProductType()) {
      case CONSUMABLE:
        // check consumable sample for how to handle consumable purchases
        Log.e(TAG, "********** [IAP] No CONSUMABLE!! **********");
        break;
      case ENTITLED:
        // check entitlement sample for how to handle consumable purchases
        Log.e(TAG, "********** [IAP] No ENTITLED!! **********");
        break;
      case SUBSCRIPTION:
        handleSubscriptionPurchase(receipt, userData);
        break;
      default:
        Log.e(TAG, "********** [IAP] No such ProductType!! **********");
        break;
    }
  }

  /**
   * This method contains the business logic to fulfill the customer's
   * purchase based on the receipt received from InAppPurchase SDK's
   * {@link PurchasingListener#onPurchaseResponse} or
   * {@link PurchasingListener#onPurchaseUpdatesResponse method.
   *
   *
   * @param userData
   * @param receipt
   */
  public void handleSubscriptionPurchase(final Receipt receipt, final UserData userData) {
    try {
      if (receipt.isCanceled()) {
        Log.d(TAG, "[IAP] receipt is canceled.");
      } else {
        Log.d(TAG, "[IAP] receipt is activated.");
        //TODO: We strongly recommend that you verify the receipt on server-side.
        grantSubscriptionPurchase(receipt, userData);
      }
      return;
    } catch (final Throwable e) {
      showMessage("Purchase cannot be completed, please retry");
    }

  }

  private void grantSubscriptionPurchase(final Receipt receipt, final UserData userData) {
    Log.d(TAG, "---------- [IAP] grantSubscriptionPurchase ----------");
    final AbebeSku abbSku = AbebeSku.fromSku(receipt.getSku(), userIapData.getAmazonMarketplace());
    // Verify that the SKU is still applicable.
    if (abbSku != AbebeSku.ABB_PREMIUM_SUBS) {
      Log.d(TAG, "[IAP] The SKU [" + receipt.getSku() + "] in the receipt is not valid anymore ");
      // if the sku is not applicable anymore, call
      // PurchasingService.notifyFulfillment with status "UNAVAILABLE"
      Log.d(TAG, "---------- [IAP] notifyFulfillment(  " + receipt.getReceiptId() + "  ,  UNAVAILABLE  ) ----------");
      PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.UNAVAILABLE);
      return;
    }
    try {
      // Set the purchase status to fulfilled for your application
      //saveSubscriptionRecord(receipt, userData.getUserId());
      Log.d(TAG, "---------- [IAP] notifyFulfillment(  " + receipt.getReceiptId() + "  ,  FULFILLED  ) ----------");
      PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);

    } catch (final Throwable e) {
      // If for any reason the app is not able to fulfill the purchase,
      // add your own error handling code here.
      Log.e(TAG, "[IAP] Failed to grant entitlement purchase, with error " + e.getMessage());
    }

  }


}
