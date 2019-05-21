import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{

  rootEntryUid = "E130B099-5C18-E0899-6817-009BCAC1111E6";
  
  constructor(private router: Router){}
  ngOnInit(): void {
    const args = { queryParams: { rootEntryUid: this.rootEntryUid} }
    this.router.navigate(['/home/contentEntryList/'], args);
  }

  ngOnDestroy(): void {}
}
