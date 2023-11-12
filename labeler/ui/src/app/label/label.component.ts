import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Record } from 'src/models/record';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-label',
  templateUrl: './label.component.html',
  styleUrls: ['./label.component.scss']
})
export class LabelComponent implements OnInit {

  records: Record[] = [];

  // page = 1;
  labeler = 1;
  total = 0;

  constructor(private api: ApiService, private router: Router, private route: ActivatedRoute) {
    const index = this.route.snapshot.paramMap.get('labeler');
    // const page = this.route.snapshot.queryParamMap.get('page');
    if (index) {
      this.labeler = parseInt(index);
    }
    if (this.labeler != 1 && this.labeler != 2) {
      this.router.navigateByUrl('/');
    } else {
      // if (page) {
      //   this.page = parseInt(page);
      // }
      this.router.navigate(['.'], {
        relativeTo: this.route,
        replaceUrl: true
      })
    }
  }

  ngOnInit() {
    this.getRecords();
  }

  getRecords(): void {
    this.api.getUnlabelledRecords(this.labeler).subscribe(result =>{
      this.records = result.data;
      this.total = result.total;
      this.records.forEach((obj) => {
        obj.visible = true;
      })
    });
  }

  updateRelevant(record: Record, recordIdx: number, isRelevant: boolean): void {
    const relevant = isRelevant ? 1 : 0;
    this.api.updateRecord(record, relevant, this.labeler).subscribe((res) => {
      if (this.labeler === 1) {
        this.records[recordIdx].relevant_1 = relevant;
      } else if (this.labeler === 2) {
        this.records[recordIdx].relevant_2 = relevant;
      }
      this.records[recordIdx].visible = false;
      this.total--;
    });
  }
}
