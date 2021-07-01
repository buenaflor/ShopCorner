import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ChartDataSets, ChartType} from 'chart.js';
import {Product} from '../../../../dtos/product';
import {BaseChartDirective, Color} from 'ng2-charts';

@Component({
  selector: 'app-products-topseller-chart',
  templateUrl: './products-topseller-chart.component.html',
  styleUrls: ['./products-topseller-chart.component.scss']
})
export class ProductsTopsellerChartComponent implements OnInit {

  @Input() products: Product[];
  @ViewChild(BaseChartDirective) chart: BaseChartDirective;

  public chartOptions = {
    scaleShowVerticalLines: false,
    responsive: true,
    scales: {
      yAxes: [
        {
          id: 'y-axis-0',
          position: 'left',
          ticks: {
            beginAtZero: true
          }
        },
        {
          id: 'y-axis-1',
          position: 'right',
          gridLines: {
            color: 'rgba(0,0,160,1)',
          },
          ticks: {
            fontColor: 'rgba(0,0,110,1)',
            beginAtZero: true
          }
        }
      ]
    },
    legend: {
      onClick: (e, i) => {
        if (i.text === 'Verkäufe') {
          this.sortBySales();
        } else if (i.text === 'Umsatz (in €)') {
          this.sortByAmounts();
        }
      }
    }
  };
  public chartLabels = [];
  public chartType: ChartType = 'bar';
  public chartLegend = true;
  chartData: ChartDataSets[];
  public chartColors: Color[] = [
    {
      backgroundColor: 'rgba(200,20,30,0.5)'
    },
    {
      backgroundColor: 'rgba(27,23,160,0.5)'
    }
  ];
  topTen = true;
  sortByAmount = true;
  error = false;
  errorMessage = '';

  constructor() { }

  ngOnInit(): void {
    this.update(this.products);
  }

  viewTop() {
    if (!this.topTen) {
      this.topTen = true;
      this.update(this.products);
    }
  }

  viewAll() {
    if (this.topTen) {
      this.topTen = false;
      this.update(this.products);
    }
  }

  sortBySales() {
    if (this.sortByAmount) {
      this.sortByAmount = false;
      this.update(this.products);
    }
  }

  sortByAmounts() {
    if (!this.sortByAmount) {
      this.sortByAmount = true;
      this.update(this.products);
    }
  }

  update(products: Product[]) {
    this.products = products;
    this.chartLabels = [];
    const tempSales = [];
    const tempAmount = [];
    if (this.sortByAmount) {
      this.products.sort((b, a) => (a.saleCount * a.price) - (b.saleCount * b.price));
    } else {
      this.products.sort((b, a) => parseFloat(a.saleCount) - parseFloat(b.saleCount));
    }
    for (const product of this.products) {
      if (this.topTen && tempSales.length === 10) {
        break;
      }
      tempSales.push(product.saleCount);
      tempAmount.push(product.saleCount * product.price);
      this.chartLabels.push(product.name);
    }
    for (let i = 0; i < tempAmount.length; i++) {
      tempAmount[i] = Math.round(tempAmount[i] * 100) / 100;
    }
    this.chartData = [{data: tempAmount, label: 'Umsatz (in €)'}, {data: tempSales, label: 'Verkäufe', yAxisID: 'y-axis-1'}];
    setTimeout(() => {
      if (this.chart && this.chart.chart && this.chart.chart.config) {
        this.chart.chart.config.data.labels = this.chartLabels;
        this.chart.chart.config.data.datasets = this.chartData;
        this.chart.chart.update();
      }
    });
  }
}
