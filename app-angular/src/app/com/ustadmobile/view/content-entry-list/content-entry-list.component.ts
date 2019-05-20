import { Component, OnInit } from '@angular/core';
import { dataSample } from '../../util/UmDataSample';
import { environment } from 'src/environments/environment.prod';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements OnInit {
  entries = dataSample;
  env = environment;

  constructor() { }

  ngOnInit() {

  }

}
