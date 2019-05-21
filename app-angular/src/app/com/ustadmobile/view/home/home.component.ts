import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  env = environment;

  constructor(private router: Router, private location: Location) { }

  ngOnInit() {}

  goBack(){
    console.log(this.location)
    this.location.back();
  }

  ngOnDestroy(): void {}

}
