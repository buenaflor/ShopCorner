import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../global/globals';
import {Observable} from 'rxjs';
import {Order} from '../dtos/order';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private orderBaseURI: string = this.globals.backendUri + '/orders';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /** Places a new order
   *
   * @param order dto containing information on the order
   * @return the order dto as returned from the backend
   */
  placeNewOrder(order: Order): Observable<Order> {
    return this.httpClient.post<Order>(this.orderBaseURI, order, {withCredentials: true});
  }

}
