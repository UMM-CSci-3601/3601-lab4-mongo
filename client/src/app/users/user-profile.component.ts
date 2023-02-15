import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { User } from './user';
import { UserService } from './user.service';
import { first, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  user: User;

  constructor(private route: ActivatedRoute, private userService: UserService) { }

  ngOnInit(): void {
    // The map and switchMap are each steps in the pipeline...
    // the map step pays attention to ParamMap
    // the result from the map step is the id string
    // that gets used by the switchMap, which was expecting a string...
    // it uses that string to get an Observable<User>
    //
    this.route.paramMap.pipe(
      // Hey! The paramMap changed... map the paramMap into the id
      map((paramMap: ParamMap) => paramMap.get('id')),
      // maps the Observable<string> (i.e., the id) into the Observable<User>
      // an observable... for each id that comes in, process and return an observable of all the results from that thing that cme in
      switchMap((id: string) => this.userService.getUserById(id)),
      // We only ever want the first value from the paramMap `Observable`;
      // limiting what we return here to that first value ensures that this
      // pipeline terminates and the subscription is garbage collected.
      first()
    ).subscribe({
      next: user => this.user = user,
      // This is terrible error handling; we should tell the user something.
      error: err => console.log('Error getting a user for user profile' + err),
      complete: () => console.log('We got a new user, and we are done!'),
    }); // when something happens, update our user field
  }
}
