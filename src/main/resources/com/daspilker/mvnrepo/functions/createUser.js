/*
   Copyright 2012 Daniel A. Spilker

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

function createUser(username, password) {
    var salt = sha256(Random.randInt(Math.pow(2, 32)).toString(16));
    var hashedPassword = sha256(password + '{' + salt + '}');

    db.users.insert({
        'username':username,
        'password':hashedPassword,
        'authorities':['ROLE_USER'],
        'salt':salt
    });
}
