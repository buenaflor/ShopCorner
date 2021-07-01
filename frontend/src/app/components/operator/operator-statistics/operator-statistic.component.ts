import {Component, OnInit, ViewChild} from '@angular/core';
import {Invoice} from '../../../dtos/invoice';
import {InvoiceService} from '../../../services/invoice/invoice.service';
import {InvoiceType} from '../../../dtos/invoiceType.enum';
import {LineChartComponent} from './line-chart/line-chart.component';
import {PieChartComponent} from './pie-chart/pie-chart.component';
import {BarChartComponent} from './bar-chart/bar-chart.component';
import {Product} from '../../../dtos/product';
import {Category} from '../../../dtos/category';
import {ProductService} from '../../../services/product/product.service';
import {CategoryService} from '../../../services/category/category.service';
import {ProductsTopsellerChartComponent} from './products-topseller-chart/products-topseller-chart.component';

@Component({
  selector: 'app-operator-statistics',
  templateUrl: './operator-statistic.component.html',
  styleUrls: ['./operator-statistic.component.scss']
})
export class OperatorStatisticComponent implements OnInit {

  @ViewChild(LineChartComponent) lineChild: LineChartComponent;
  @ViewChild(PieChartComponent) pieChild: PieChartComponent;
  @ViewChild(BarChartComponent) barChild: BarChartComponent;
  @ViewChild(ProductsTopsellerChartComponent) productTopseller: ProductsTopsellerChartComponent;

  error = false;
  errorMessage = '';
  invoices: Invoice[];
  products: Product[];
  categories: Category[];
  selected: number;
  invoiceType = InvoiceType.operator;
  end = new Date();
  start = new Date();
  startPicker: string;
  endPicker: string;
  loaded = false;
  showProducts = false;

  constructor(private invoiceService: InvoiceService, private productService: ProductService, private categoryService: CategoryService) {
  }

  ngOnInit(): void {
    this.start.setDate(this.start.getDate() - 91);
    this.startPicker = this.start.toISOString().split('T')[0];
    this.endPicker = this.end.toISOString().split('T')[0];
    this.loadInvoicesForTime();
  }

  update() {
    this.start = new Date(this.startPicker);
    this.end = new Date(this.endPicker);
    this.loadInvoicesForTime();
  }

  getMonth(month: string) {
    this.start = new Date(month);
    this.end = new Date(this.start);
    this.end.setMonth(this.end.getMonth() + 1);
    this.end.setDate(this.end.getDate() - 1);
    this.startPicker = this.start.toISOString().split('T')[0];
    if (this.end.getMonth() === 2) {
      const tempDate = new Date(this.end);
      tempDate.setDate(tempDate.getDate() + 1);
      this.endPicker = tempDate.toISOString().split('T')[0];
    } else {
      this.endPicker = this.end.toISOString().split('T')[0];
    }
    this.loadInvoicesForTime();
  }

  updateInvoiceChildren() {
    this.lineChild.update(this.start, this.end, this.invoices);
    this.barChild.update(this.start, this.end, this.invoices);
    this.pieChild.update(this.invoices);
  }

  updateProductChildren() {
    this.productTopseller.update(this.products);
  }

  viewProducts() {
    if (this.products === undefined) {
      this.loaded = false;
      this.fetchCategories();
    }
    this.showProducts = true;
  }

  changeCategory() {
    this.fetchProducts();
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  /**
   * calls on Service class to fetch all customer accounts from backend
   */
  private loadInvoicesForTime() {
    this.invoiceService.getAllInvoicesByDate(this.start, this.end).subscribe(
      (invoices: Invoice[]) => {
        this.invoices = invoices;
        if (this.loaded === true) {
          this.updateInvoiceChildren();
        } else {
          this.loaded = true;
        }
      },
      error => {
        this.error = true;
        this.errorMessage = error;
      }
    );
  }

  private fetchCategories() {
    this.categoryService.getCategories().subscribe(
      (categories: Category[]) => {
        this.categories = categories;
        this.selected = this.categories[0].id;
        this.fetchProducts();
      }, error => {
        this.error = true;
        this.errorMessage = error;
      }
    );
  }

  private fetchProducts() {
    this.productService.getProductsByCategory(this.selected).subscribe(
      (products: Product[]) => {
        this.products = products;
        if (this.loaded === true) {
          this.updateProductChildren();
        } else {
          this.loaded = true;
        }
      }, error => {
        this.error = true;
        this.errorMessage = error;
      });
  }
}
