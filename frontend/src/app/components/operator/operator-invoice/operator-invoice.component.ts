import {Component, OnInit} from '@angular/core';
import pdfMake from 'pdfmake/build/pdfmake';
import pdfFonts from 'pdfmake/build/vfs_fonts';
import {Product} from '../../../dtos/product';
import {Invoice} from '../../../dtos/invoice';
import {FormBuilder, FormGroup, FormArray, Validators, FormControl} from '@angular/forms';
import {InvoiceService} from '../../../services/invoice.service';

import {formatDate} from '@angular/common';

pdfMake.vfs = pdfFonts.pdfMake.vfs;

@Component({
  selector: 'app-operator-invoice',
  templateUrl: './operator-invoice.component.html',
  styleUrls: ['./operator-invoice.component.scss']
})
export class OperatorInvoiceComponent implements OnInit {
  newInvoiceForm: FormGroup;
  submitted = false;
  error = false;
  errorMessage = '';
  invoiceDto: Invoice;

  mapItem = {};
  map = [];

  constructor(private invoiceService: InvoiceService, private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.newInvoiceForm = this.formBuilder.group({
      items: new FormArray([])
    });
    this.addProductOnClick();
  }

  get f() {
    return this.newInvoiceForm.controls;
  }

  get t() {
    return this.f.items as FormArray;
  }

  addProductOnClick() {
    this.t.push(this.formBuilder.group({
      name: ['', Validators.required],
      price: ['', [Validators.required]],
      quantity: ['', [Validators.required]]
    }));
  }

  onSubmit() {
    this.submitted = true;
    if (this.newInvoiceForm.invalid) {
      return;
    }
    this.creatInvoiceDto();
    this.generatePdf();
    // this.generatePdf();
    // this.addInvoice();
  }

  addInvoice() {
    this.invoiceService.createInvoice(this.invoiceDto).subscribe(
      (invoice: Invoice) => {
        console.log(invoice);
      },
      (error) => {
        this.defaultServiceErrorHandling(error);
      });
  }

  creatInvoiceDto() {
    this.invoiceDto = new Invoice();
    let amount = 0;
    for (const item of this.t.controls) {
      const product = new Product();
      for (let j = 0; j < item.value.quantity; j++) {
        product.name = item.value.name;
        product.price = item.value.price;
        this.invoiceDto.invoiceItems.push(product);
      }
      amount += item.value.price * item.value.quantity;
      this.mapItem['product'] = product;
      this.mapItem['quantity'] = item.value.quantity;
      this.map.push(this.mapItem);
    }
    this.invoiceDto.amount = amount;
    this.invoiceDto.date = formatDate(new Date(), 'dd.MM.yyyy HH:mm:ss', 'en');
  }

   generatePdf() {
     const docDefinition = {
       content: [
         {
           text: 'ELECTRONIC SHOP',
           fontSize: 16,
           alignment: 'center',
           color: '#047886'
         },
         {
           text: 'INVOICE',
           fontSize: 20,
           bold: true,
           alignment: 'center',
           decoration: 'underline',
           color: 'skyblue'
         },
         {
           text: 'Customer Details',
           style: 'sectionHeader'
         },
         {
           columns: [
             [
               {
                 text: 'this.invoice.customerName',
                 bold: true
               },
               { text: 'this.invoice.address' },
               { text: 'this.invoice.email' },
               { text: 'this.invoice.contactNo' }
             ],
             [
               {
                 text: `Date: ${new Date().toLocaleString()}`,
                 alignment: 'right'
               },
               {
                 text: `Bill No : ${((Math.random() * 1000).toFixed(0))}`,
                 alignment: 'right'
               }
             ]
           ]
         },
         {
           text: 'Order Details',
           style: 'sectionHeader'
         },
         {
           table: {
             headerRows: 1,
             widths: ['*', 'auto', 'auto', 'auto'],
             body: [
               ['Product', 'Price', 'Quantity', 'Amount'],

             ]
           }
         },
         {
           text: 'Additional Details',
           style: 'sectionHeader'
         },
         {
           text: 'this.invoice.additionalDetails',
           margin: [0, 0 , 0, 15]
         },
         {
           columns: [
             [{ qr: `www.facebook.com`, fit: '50' }],
             [{ text: 'Signature', alignment: 'right', italics: true}],
           ]
         },
         {
           text: 'Terms and Conditions',
           style: 'sectionHeader'
         },
         {
           ul: [
             'Order can be return in max 10 days.',
             'Warrenty of the product will be subject to the manufacturer terms and conditions.',
             'This is system generated invoice.',
           ],
         }
       ],
       styles: {
         sectionHeader: {
           bold: true,
           decoration: 'underline',
           fontSize: 14,
           margin: [0, 15, 0, 15]
         }
       }
     };


     for (const item of this.map) {
       docDefinition['content'][5].table.body.push([item.product.name, item.product.price,
         item.quantity, item.product.price * item.quantity]);
     }
     pdfMake.createPdf(docDefinition).open();
   }



  onReset() {
    this.submitted = false;
    this.newInvoiceForm.reset();
    this.t.clear();
    this.addProductOnClick();
  }

  deleteProductFromInvoice(id: number) {
    if (this.t.length > 1) {
      this.t.removeAt(id);
    } else {
      this.errorHandling('Es können nicht alle Elemente einer Rechnung gelöscht werden');
    }
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  /**
   * @param error
   * @private
   */
  private errorHandling(errorMessage: string) {
    if (errorMessage !== null) {
      this.error = true;
      this.errorMessage = errorMessage;
    } else {
      this.error = false;
    }
  }

  /**
   * @param error
   * @private
   */
  private defaultServiceErrorHandling(error: any) {
    console.log(error);
    this.error = true;
    if (error.status === 0) {
      // If status is 0, the backend is probably down
      this.errorMessage = 'The backend seems not to be reachable';
    } else if (error.error.message === 'No message available') {
      // If no detailed error message is provided, fall back to the simple error name
      this.errorMessage = error.error.error;
    } else {
      this.errorMessage = error.error.message;
    }
  }

}
