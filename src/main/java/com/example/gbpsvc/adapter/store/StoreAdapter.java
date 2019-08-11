package com.example.gbpsvc.adapter.store;

import com.example.gbpsvc.adapter.AdapterException;
import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;

public interface StoreAdapter {
    /**
     * Get price of <b>sku</b> from store <b>storeId</b>. Using REST API, therefore it  is a normal HTTP blocking until
     * the response is get back.
     *
     * Note This is a normal synchronous HTTP (REST) call to get the unit price for the SKU.
     *
     * @param storeId Store ID of store to query.
     * @param sku     SKU identifier of product whose price is required.
     * @return SkuPrice.
     * @throws AdapterException Unchecked exception carrying error messages out of it, cause possibilities are:
     *          <table>
     *              <header>
     *                  <tr>
     *                      <th></th>
     *                  </tr>
     *              </header>
     *          </table>
     *         None.
     *         HttpClientErrorException
     *         ResourceAccessException
     */
    StoreSkuPriceDTO getPriceByStoreIdAndSku(String storeId, String sku);
}
