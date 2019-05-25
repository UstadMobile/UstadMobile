import {Component,OnInit} from '@angular/core';
import {dataSample} from '../../util/UmDataSample';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { com as com_core} from 'core';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements com_core.ustadmobile.core.view.ContentEntryListFragmentView ,OnInit {
  
  entries = [];
  env = environment;
  currentEntryUid = "";
  private readonly args: Params = null;
  private readonly context: UmContextWrapper;
  private presenter: com_core.ustadmobile.core.controller.ContentEntryListFragmentPresenter;

  constructor(private router: Router, private route: ActivatedRoute) {
    this.args = this.route.snapshot.queryParamMap;
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(route);
    this.route.queryParams.subscribe(args => {
      this.currentEntryUid = args["parentUid"];
      this.entries = dataSample[this.currentEntryUid];
    });
  }

  ngOnInit() {
    this.presenter = new com_core.ustadmobile.core.controller.ContentEntryListFragmentPresenter(this.context, this.args, this);
  }

  ngOnDestroy(){
    this.presenter.onDestroy();
  }

  navigate(entry) {
    const contentEntry = new com_core.ustadmobile.lib.db.entities.ContentEntry();
    contentEntry.setContentEntryUid(entry.entry_uid);
    contentEntry.setTitle(entry.entry_name);
    contentEntry.setThumbnailUrl(entry.entry_image);
    contentEntry.setDescription(entry.entry_description);
    contentEntry.setPublisher("CK12");
    contentEntry.setAuthor("borrachera");
    contentEntry.setPrimaryLanguageUid(345);
    contentEntry.setSourceUrl("khan-id://x7d37671e");
    contentEntry.setLeaf(true);
    const rootEntry = entry.entry_root === true;
    const basePath = rootEntry ? 'contentEntryList/' :'contentEntryDetail';

    const args: any = {
      relativeTo: this.route,
      queryParams: rootEntry ? {parentUid: entry.entry_uid}: {entryUid: entry.entry_uid},
      queryParamsHandling: 'merge',
    };
   // this.router.navigate([basePath], args);
    this.presenter.handleContentEntryClicked(contentEntry);
  }
}
