import {Injectable} from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class Globals {
  readonly backendUri: string = Globals.findBackendUrl();

  readonly roles = {
    admin: 'ADMIN',
    employee: 'EMPLOYEE',
    customer: 'CUSTOMER',
  };

  readonly requestParamKeys = {
    pagination: {
      page: 'page',
      pageCount: 'pageCount',
    },
    products: {
      name: 'name',
      categoryId: 'categoryId',
      sortBy: 'sortBy',
    },
    operators: {
      permissions: 'permissions',
    },
    invoice: {
      invoiceType: 'invoiceType',
    },
    paypal: {
      payerId: 'payerId',
      paymentId: 'paymentId'
    },
    date: {
      start: 'start',
      end: 'end'
    }
  };

  readonly defaultSettings = {
    title: 'ShopCorner',
    logo: 'https://i.imgur.com/zMBx1FY.png',
    bannerTitle: 'ShopCorner',
    bannerText: 'Willkommen bei ShopCorner!',
    street: 'Musterstrasse',
    houseNumber: '23',
    stairNumber: 3,
    doorNumber: '15A',
    postalCode: 1220,
    city: 'Wien',
    phoneNumber: '+436991234567',
    email: 'musteremail@shop.com',
  };

  private static findBackendUrl(): string {
    if (window.location.port === '4200') { // local `ng serve`, backend at localhost:8080
      return 'http://localhost:8080/api/v1';
    } else {
      // assume deployed somewhere and backend is available at same host/port as frontend
      return window.location.protocol + '//' + window.location.host + window.location.pathname + 'api/v1';
    }
  }
}


