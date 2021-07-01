import {Component, OnInit} from '@angular/core';
import {Invoice} from '../../../dtos/invoice';
import {InvoiceService} from '../../../services/invoice/invoice.service';
import {Pagination} from '../../../dtos/pagination';
import {InvoiceType} from '../../../dtos/invoiceType.enum';
import {NgdbModalActionComponent} from '../../common/ngbd-modal-action/ngdb-modal-action.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {faPlusCircle} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-operator-invoice',
  templateUrl: './operator-invoice.component.html',
  styleUrls: ['./operator-invoice.component.scss']
})
export class OperatorInvoiceComponent implements OnInit {
  toggleForm = false;
  toggleDetailView = false;

  invoices: Invoice[];
  page = 0;
  pageSize = 15;
  collectionSize = 0;
  error = false;
  errorMessage = '';
  detailViewInvoice: Invoice;
  invoiceType = InvoiceType.operator;
  onCanceledWindow = false;

  faPlusCircle = faPlusCircle;

  constructor(private invoiceService: InvoiceService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    this.loadInvoicesForPage();
  }

  toggleSide() {
    if (!this.toggleDetailView) {
      this.toggleForm = !this.toggleForm;
    } else {
      this.toggleForm = false;
    }
    this.toggleDetailView = false;
  }

  vanishError() {
    this.error = false;
  }

  /**
   * goes to next page if not on the last page
   */
  nextPage() {
    if ((this.page + 1) * this.pageSize < this.collectionSize) {
      this.page += 1;
      this.loadInvoicesForPage();
    }
  }

  /**
   * goes to previous page if not on the first page
   */
  previousPage() {
    if (this.page > 0) {
      this.page -= 1;
      this.loadInvoicesForPage();
    }
  }

  /**
   * toggles to the detailView.
   *
   * @param invoice destination invoice
   */
  getDetailedView(invoice: Invoice) {
    this.toggleDetailView = true;
    this.toggleForm = false;
    this.detailViewInvoice = invoice;
  }

  isCanceled(invoice: Invoice) {
    if (this.onCanceledWindow) {
      return false;
    }
    if (invoice !== undefined) {
      return invoice.invoiceType === InvoiceType.canceled;
    }
    return false;
  }

  isDetailedInvoiceCanceled() {
    if (this.detailViewInvoice !== undefined) {
      return this.detailViewInvoice.invoiceType === InvoiceType.canceled;
    }
    return false;
  }

  canceledInvoice() {
    this.invoiceService.setInvoiceCanceled(this.detailViewInvoice).subscribe(() => {
      this.loadInvoicesForPage();
    }, (error) => {
      this.error = true;
      this.errorMessage = error;
    });
    this.loadInvoicesForPage();
    this.toggleSide();
    this.showAll();
  }

  showAll() {
    this.invoiceType = InvoiceType.operator;
    this.invoices = [];
    this.resetPage();
    this.loadInvoicesForPage();
    this.onCanceledWindow = false;
  }

  showCustomer() {
    this.invoiceType = InvoiceType.customer;
    this.invoices = [];
    this.resetPage();
    this.loadInvoicesForPage();
    this.onCanceledWindow = false;

  }

  showCanceled() {
    this.invoiceType = InvoiceType.canceled;
    this.invoices = [];
    this.resetPage();
    this.loadInvoicesForPage();
    this.onCanceledWindow = true;
  }

  resetPage() {
    this.page = 0;
    this.pageSize = 15;
    this.collectionSize = 0;
  }

  showToolTip(invoice: Invoice) {
    let toolTipText = '';
    switch (invoice.invoiceType) {
      case InvoiceType.canceled:
        toolTipText = 'Stronierte Rechnung';
        break;
      case InvoiceType.customer:
        toolTipText = 'Kunden Rechnung';
        break;
      case InvoiceType.operator:
        toolTipText = 'Im Geschäft erstellte Rechnung';
        break;
    }
    return toolTipText;
  }

  attemptToCancelInvoiceModal() {
    const modalRef = this.modalService.open(NgdbModalActionComponent);
    modalRef.componentInstance.title = 'Stornieren';
    modalRef.componentInstance.body = 'Wollen Sie die Rechnung unwiderruflich storinieren?';
    modalRef.componentInstance.actionButtonTitle = 'Stornieren';
    modalRef.componentInstance.actionButtonStyle = 'danger';
    modalRef.componentInstance.action = () => {
      this.canceledInvoice();
    };
  }

  /**
   * calls on Service class to fetch all customer accounts from backend
   */
  private loadInvoicesForPage() {
    this.invoiceService.getAllInvoicesForPage(this.page, this.pageSize, this.invoiceType).subscribe(
      (paginationDto: Pagination<Invoice>) => {
        this.invoices = paginationDto.items;
        this.pageSize = paginationDto.pageSize;
        this.collectionSize = paginationDto.totalItemCount;
      },
      error => {
        this.error = true;
        this.errorMessage = error;
      }
    );
  }


}
