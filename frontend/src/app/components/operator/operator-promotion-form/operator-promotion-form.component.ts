import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Promotion} from '../../../dtos/promotion';
import {PromotionService} from '../../../services/promotion/promotion.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Component({
  selector: 'app-operator-promotion-form',
  templateUrl: './operator-promotion-form.component.html',
  styleUrls: ['./operator-promotion-form.component.scss']
})
export class OperatorPromotionFormComponent implements OnInit {
  @Output() operatorPromotionFormComponentSuccess: EventEmitter<any> = new EventEmitter();
  newPromotion: Promotion;
  today = new Date(Date.now());
  promotionForm: FormGroup;
  error = false;
  errorMessage = '';
  form;
  constructor(private promotionService: PromotionService, private formBuilder: FormBuilder) {
  }

  private static addLeadingZero(num: number): string {
    return num < 10 ? '0' + num : num.toString();
  }

  ngOnInit(): void {

    this.promotionForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      discount: [0, Validators.required],
      time: [{hour: 0, minute: 0}, Validators.required],
      expirationDate: [{
        day: this.today.getDate(),
        month: this.today.getMonth() + 1,
        year: this.today.getFullYear()
      }, Validators.required],
      code: ['', Validators.required],
      minimum: [0, Validators.required]
    });
  }

  addNewPromotion() {
    const expDate = OperatorPromotionFormComponent.addLeadingZero(this.promotionForm.controls.expirationDate.value.year) + '-'
      + OperatorPromotionFormComponent.addLeadingZero(this.promotionForm.controls.expirationDate.value.month)
      + '-' + OperatorPromotionFormComponent.addLeadingZero(this.promotionForm.controls.expirationDate.value.day) + 'T'
      + OperatorPromotionFormComponent.addLeadingZero(this.promotionForm.controls.time.value.hour) + ':'
      + OperatorPromotionFormComponent.addLeadingZero(this.promotionForm.controls.time.value.minute) + ':' + '00';
    this.newPromotion = new Promotion(0, this.promotionForm.controls.name.value, this.promotionForm.controls.discount.value,
      '', expDate, this.promotionForm.controls.code.value, this.promotionForm.controls.minimum.value);
    this.promotionService.addPromotion(this.newPromotion).subscribe(() => {
      this.onSuccess(false);
    }, error => {
      this.error = true;
      this.errorMessage = error;
    });
  }

  onSuccess(val: boolean) {
    this.operatorPromotionFormComponentSuccess.emit(val);
  }

  vanishError() {
    this.error = false;
  }
}
