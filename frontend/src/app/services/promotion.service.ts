import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Globals} from '../global/globals';
import {OperatorAuthService} from './auth/operator-auth.service';
import {Observable} from 'rxjs';
import {Pagination} from '../dtos/pagination';
import {Promotion} from '../dtos/promotion';

@Injectable({
  providedIn: 'root'
})
export class PromotionService {
  private promotionBaseUri: string = this.globals.backendUri + '/promotions';

  constructor(private httpClient: HttpClient, private globals: Globals, private operatorAuthService: OperatorAuthService) {
  }

  /**
   * Register a new customer.
   *
   * @param promotion The promotion dto to register
   * @return The promotion dto as received from the backend
   */
  addPromotion(promotion: Promotion): Observable<Promotion> {
    console.log('Create new Customer', promotion);
    return this.httpClient.post<Promotion>(this.promotionBaseUri, promotion);
  }

  /**
   * Retrieve a page of customers from the backend.
   *
   * @param page the number of the page to fetch
   * @param pageCount the size of the page to be fetched
   * @return The promotions retrieved from the backend
   */
  getAllPromotionsForPage(page: number, pageCount: number): Observable<Pagination<Promotion>> {
    console.log('Get promotions for page', page);
    return this.httpClient.get<Pagination<Promotion>>(
      this.promotionBaseUri + '?page=' + page + '&page_count=' + pageCount,
      {headers: this.getHeadersForOperator()}
    );
  }


  private getHeadersForOperator(): HttpHeaders {
    return new HttpHeaders()
      .set('Authorization', `Bearer ${this.operatorAuthService.getToken()}`);
  }
}
