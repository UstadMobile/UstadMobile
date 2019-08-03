import { Component, OnInit } from '@angular/core';
import entity from 'UstadMobile-lib-database-entities';
import db from 'UstadMobile-lib-database';
import ktorclientserial from 'ktor-ktor-client-serialization';
import core from 'UstadMobile-core';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{

  ngOnInit(): void {
    //entities test
    console.log(new entity.com.ustadmobile.lib.db.entities.ContentEntry()) 

    //db test
    console.log(db.com.ustadmobile.core.db.UmAppDatabase.Companion)

    console.log(ktorclientserial)

    //test core (causing issues)
    console.log(core)
  }
  title = 'app-test';
}
