import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, switchMap } from 'rxjs';

import { Record, RecordResponse } from './../models/record';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  url = 'http://localhost:3000';

  constructor(private http: HttpClient) { }

  getRecords(page: number = 1): Observable<Record[]> {
    return this.http.get<any[]>(`${this.url}/comments?page=${page}`, {
      headers: {
        'Accept': 'application/json'
      }
    });
  }

  getUnlabelledRecords(label: number): Observable<RecordResponse> {
    return this.http.get<RecordResponse>(`${this.url}/unlabelled-comments?label=${label}`, {
      headers: {
        'Accept': 'application/json'
      }
    });
  }

  updateRecord(record: Record, relevant: number, labeler: number): Observable<any> {
    const payload = {
      labeler: labeler,
      relevant: relevant
    };
    return this.http.patch(`${this.url}/comments/${record.id}`, payload);
  }
}
