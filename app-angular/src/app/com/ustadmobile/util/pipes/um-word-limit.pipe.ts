import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'umWordLimit'
})
export class UmWordLimitPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    let result = value || '';

    if (value) {
      const words = value.split(/\s+/);
      if (words.length > Math.abs(args)) {
        if (args < 0) {
          args *= -1;
          result = "…" + words.slice(words.length - args, words.length).join(' ');
        } else {
          result = words.slice(0, args).join(' ') + "…";
        }
      }
    }
    return result;
  }

}
