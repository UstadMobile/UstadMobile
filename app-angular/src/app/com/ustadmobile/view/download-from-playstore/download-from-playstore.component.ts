import { Component, OnInit, OnDestroy } from '@angular/core';
import { MzBaseModal } from 'ngx-materialize';

@Component({
  selector: 'app-download-from-playstore',
  templateUrl: './download-from-playstore.component.html',
  styleUrls: ['./download-from-playstore.component.css']
})

export class DownloadFromPlaystoreComponent extends MzBaseModal implements OnInit, OnDestroy {

  barge_img =""

  avatar_image = ""

  public modalOptions: Materialize.ModalOptions = {
    dismissible: false,
    opacity: .5, 
    inDuration: 300,
    outDuration: 200, 
    startingTop: '100%', 
    endingTop: '30%' 
  }; 

  constructor() { 
    super();
  }

  ngOnInit() {
    const basePath = window.location.origin + window.location.pathname;
    this.barge_img =  basePath + "assets/images/get_from_playtore.jpg";
    this.avatar_image = basePath + "assets/images/downloading_data.png";
  }

  openPayStore(){
    window.open('https://play.google.com/store/apps/details?id=com.toughra.ustadmobile',"_self")
  }

  ngOnDestroy(): void {}

}
