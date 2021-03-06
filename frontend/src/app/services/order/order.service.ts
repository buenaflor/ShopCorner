import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {Observable} from 'rxjs';
import {Order} from '../../dtos/order';
import {CancellationPeriod} from '../../dtos/cancellationPeriod';
import {OperatorAuthService} from '../auth/operator-auth.service';
import {CustomerAuthService} from '../auth/customer-auth.service';
import {Pagination} from '../../dtos/pagination';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private orderBaseURI: string = this.globals.backendUri + '/orders';

  constructor(private httpClient: HttpClient, private globals: Globals,
              private operatorAuthService: OperatorAuthService,
              private customerAuthService: CustomerAuthService) {
  }

  /**
   * Gets the order specified by the id
   *
   * @param id the id of the order to be retrieved from the database
   * @return An observable with the requested order
   */
  getOrderById(id: number) {
    return this.httpClient.get<Order>(this.orderBaseURI + '/' + id, {
      headers: this.getHeadersForCustomer()
    });
  }

  /** Places a new order
   *
   * @param order dto containing information on the order
   * @return the order dto as returned from the backend
   */
  placeNewOrder(order: Order): Observable<Order> {
    return this.httpClient.post<Order>(this.orderBaseURI, order, {withCredentials: true, headers: this.getHeadersForCustomer()});
  }

  /** sets the cancellation period for orders.
   *
   * @param cancellationPeriod the dto containing the information on the cancellation period
   * @return the dto as returned from the backend
   */
  setCancellationPeriod(cancellationPeriod: CancellationPeriod): Observable<CancellationPeriod> {
    return this.httpClient.put<CancellationPeriod>(this.orderBaseURI + '/settings',
      cancellationPeriod, {headers: this.getHeadersForOperator()});
  }

  /**
   * Set order entry to canceled.
   *
   * @param order to be updated
   * @return order updated from the given invoice and invoice entry
   */
  setOrderCanceled(order: Order): Observable<Order> {
    return this.httpClient.patch<Order>(this.orderBaseURI + '/' + order.id, order, {headers: this.getHeadersForCustomer()});
  }

  /** gets the cancellation period from the backend
   *
   * @return the cancellation period
   */
  getCancellationPeriod(): Observable<CancellationPeriod> {
    return this.httpClient.get<CancellationPeriod>(this.orderBaseURI + '/settings');
  }

  /**
   * fetches all orders from the backend
   *
   * @param page that is needed
   * @param pageCount amount of orders per page
   * @return the page with orders
   */
  getOrdersPage(page: number, pageCount: number): Observable<Pagination<Order>> {
    console.log('Get orders for page: ', page);
    const params = new HttpParams()
      .set(this.globals.requestParamKeys.pagination.page, String(page))
      .set(this.globals.requestParamKeys.pagination.pageCount, String(pageCount));

    return this.httpClient.get<Pagination<Order>>(this.orderBaseURI, {params, headers: this.getHeadersForOperator()});
  }

  private getHeadersForOperator(): HttpHeaders {
    return new HttpHeaders()
      .set('Authorization', `Bearer ${this.operatorAuthService.getToken()}`);
  }

  private getHeadersForCustomer(): HttpHeaders {
    return new HttpHeaders()
      .set('Authorization', `Bearer ${this.customerAuthService.getToken()}`);
  }
}
