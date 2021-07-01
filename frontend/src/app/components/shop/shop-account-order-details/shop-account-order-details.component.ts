import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {OrderService} from '../../../services/order/order.service';
import {Order} from '../../../dtos/order';
import {InvoiceItem} from '../../../dtos/invoiceItem';
import {Product} from '../../../dtos/product';
import {CartItem} from '../../../dtos/cartItem';
import {ProductService} from '../../../services/product/product.service';
import {Globals} from '../../../global/globals';
import {CartGlobals} from '../../../global/cartGlobals';
import {CartService} from '../../../services/cart/cart.service';
import {MeService} from '../../../services/me/me.service';
import {NgdbModalActionComponent} from '../../common/ngbd-modal-action/ngdb-modal-action.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Customer} from '../../../dtos/customer';
import {Address} from '../../../dtos/address';
import {Invoice} from '../../../dtos/invoice';

@Component({
  selector: 'app-shop-account-order-details',
  templateUrl: './shop-account-order-details.component.html',
  styleUrls: ['./shop-account-order-details.component.scss']
})
export class ShopAccountOrderDetailsComponent implements OnInit {
  error = false;
  errorMessage = '';

  order: Order;
  items: InvoiceItem[];
  day: string;
  month: string;
  year: string;
  time: string;

  isCanceled = false;
  isCancelable = false;

  constructor(private route: ActivatedRoute, private orderService: OrderService,
              private router: Router,
              private productService: ProductService,
              private globals: Globals,
              private cartGlobals: CartGlobals,
              private cartService: CartService,
              private meService: MeService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    const address = new Address(0, '', 0, '', 0, '');
    const invoice = new Invoice();
    invoice.date = '';
    invoice.amount = 0;
    this.order = new Order(0, invoice, new Customer(0, '', '', '', '', address, ''), null);
    const id = +this.route.snapshot.paramMap.get('id');

    this.orderService.getOrderById(id)
      .subscribe(order => {
        this.order = order;
        if (order.invoice.invoiceType === 'canceled') {
          this.isCanceled = true;
        }
      }, _ => {
        this.router.navigate(['404']);
      }, () => {
        this.items = this.order.invoice.items;
        this.time = this.order.invoice.date.substring(11);
        this.day = this.order.invoice.date.substring(8, 10);
        this.month = this.order.invoice.date.substring(5, 7);
        this.year = this.order.invoice.date.substring(0, 4);
      });
    this.cancellationPeriod();
  }

  vanishError() {
    this.error = false;
  }

  /**
   * Adds the specified product to the cart.
   */
  addToCart(product: Product) {
    const index = this.cartGlobals.containsProductAtIndex(product);
    if (index === -1) {
      this.cartGlobals.addToCart(product);
      this.cartService.addProductsToCart(new CartItem(product.id, 1)).subscribe((item) => {
        this.cartGlobals.updateCartItem(item);

      }, (error) => {
        this.error = true;
        this.errorMessage = error;
      });

    } else {
      const cart = this.cartGlobals.getCart();
      const quantity = Number(cart[this.cartGlobals.containsProductAtIndex(product)]['cartItemQuantity']) + 1;
      if (quantity <= 12) {
        cart[this.cartGlobals.containsProductAtIndex(product)]['cartItemQuantity'] = quantity;
        this.cartGlobals.updateCart(product, quantity);
        const cartItem = new CartItem(product.id, quantity);
        cartItem.id = cart[this.cartGlobals.containsProductAtIndex(product)]['cartItemId'];
        this.cartService.updateToCart(cartItem).subscribe((item) => {
          this.cartGlobals.updateCartItem(item);
        }, (error) => {
          this.error = true;
          this.errorMessage = error;
        });
      }
    }
    if (!this.error) {
      this.router.navigate(['cart']).then();
    }
  }

  /**
   * Downloads the given invoice as a pdf.
   */
  downloadInvoice(id: number, date: string) {
    this.meService.getInvoiceAsPdfByIdForCustomer(id).subscribe((data) => {
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

  getImageSource(product: Product): string {
    if (product.picture != null) {
      return 'data:image/png;base64,' + product.picture;
    }
    return 'Error: no picture available';
  }


  attemptToCancelInvoiceModal() {
    const modalRef = this.modalService.open(NgdbModalActionComponent);
    modalRef.componentInstance.title = 'Stornieren';
    modalRef.componentInstance.body = 'Wollen Sie die Rechnung unwiderruflich stornieren?';
    modalRef.componentInstance.actionButtonTitle = 'Stornieren';
    modalRef.componentInstance.actionButtonStyle = 'danger';
    modalRef.componentInstance.action = () => {
      this.canceledOrder();
    };
  }

  canceledOrder() {
    this.orderService.setOrderCanceled(this.order).subscribe(() => {
      this.isCanceled = true;
    }, (error) => {
      this.error = true;
      this.errorMessage = error;
    });
  }

  cancellationPeriod() {
    this.orderService.getCancellationPeriod().subscribe((item) => {
      const date = this.order.invoice.date;
      const invoiceDate = new Date(date);
      const today = new Date();
      invoiceDate.setDate(invoiceDate.getDate() + item.days);
      this.isCancelable = invoiceDate > today;
    }, (error) => {
      this.error = true;
      this.errorMessage = error;
    });
  }
}
