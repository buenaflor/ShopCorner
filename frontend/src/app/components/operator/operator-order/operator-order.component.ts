import { Component, OnInit } from '@angular/core';
import {Order} from '../../../dtos/order';
import {Pagination} from '../../../dtos/pagination';
import {OrderService} from '../../../services/order/order.service';
import {InvoiceService} from '../../../services/invoice/invoice.service';
import {faCog} from '@fortawesome/free-solid-svg-icons';
import {OperatorAuthService} from "../../../services/auth/operator-auth.service";

@Component({
  selector: 'app-operator-order',
  templateUrl: './operator-order.component.html',
  styleUrls: ['./operator-order.component.scss']
})
export class OperatorOrderComponent implements OnInit {
  error = false;
  errorMessage = '';
  orders: Order[];
  page = 0;
  pageSize = 15;
  collectionSize = 0;
  faCog = faCog;

  constructor(private orderService: OrderService, private invoiceService: InvoiceService, public authService: OperatorAuthService) { }

  ngOnInit(): void {
    this.loadOrdersForPage();
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  /**
   * Downloads the given invoice as a pdf.
   */
  downloadInvoice(id: number, date: string) {
    this.invoiceService.getInvoiceAsPdfById(id).subscribe((data) => {
      const downloadURL = window.URL.createObjectURL(data);
      const link = document.createElement('a');
      link.href = downloadURL;
      link.download = 'invoice_' + date + '_' + id + '.pdf';
      link.click();
    }, (error) => {
      this.error = true;
      this.errorMessage = error;
    });
  }

  /**
   * goes to next page if not on the last page
   */
  nextPage() {
    if ((this.page + 1) * this.pageSize < this.collectionSize) {
      this.page += 1;
      this.loadOrdersForPage();
    }
  }

  /**
   * goes to previous page if not on the first page
   */
  previousPage() {
    if (this.page > 0) {
      this.page -= 1;
      this.loadOrdersForPage();
    }
  }

  /**
   * calls on Service class to fetch all orders from the backend
   */
  private loadOrdersForPage() {
    this.orderService.getOrdersPage(this.page, this.pageSize).subscribe(
      (paginationDto: Pagination<Order>) => {
        this.orders = paginationDto.items;
        this.collectionSize = paginationDto.totalItemCount;
      },
      error => {
        this.error = true;
        this.errorMessage = error.error;
      }
    );
  }

}
