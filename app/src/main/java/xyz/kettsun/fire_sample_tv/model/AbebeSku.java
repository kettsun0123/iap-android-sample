package xyz.kettsun.fire_sample_tv.model;

/**
 * Created by Yuji Koketsu on 2017/06/08.
 */

public enum AbebeSku {
  //The only subscription product used in this sample app
  ABB_PREMIUM_SUBS("xyz.kettsun.premium.plan", "JP");

  private final String sku;
  private final String availableMarkpetplace;

  AbebeSku(String sku, String availableMarkpetplace) {
    this.sku = sku;
    this.availableMarkpetplace = availableMarkpetplace;
  }

  /**
   * Returns the Sku string of the MySku object
   * @return
   */
  public String getSku() {
    return this.sku;
  }

  /**
   * Returns the Available Marketplace of the MySku object
   * @return
   */
  public String getAvailableMarketplace() {
    return this.availableMarkpetplace;
  }

  /**
   * Returns the MySku object from the specified Sku and marketplace value.
   * @param sku
   * @param marketplace
   * @return
   */
  public static AbebeSku fromSku(final String sku, final String marketplace) {
    if (ABB_PREMIUM_SUBS.getSku().equals(sku) && (null == marketplace || ABB_PREMIUM_SUBS.getAvailableMarketplace()
        .equals(marketplace))) {
      return ABB_PREMIUM_SUBS;
    }
    return null;
  }

}
