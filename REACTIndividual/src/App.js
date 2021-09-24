import React,{Component} from 'react';
import { HashRouter, Route, Switch } from 'react-router-dom';

//styles
import './App.css';

//imports
import Menu from './Components/Menu/Menu.js';
import Home from './Components/Home/Home.js';
import Register from './Components/User/Register';
import Login from './Components/User/Login';
import { PathsLabel } from './Components/Utils/Paths';

class App extends Component {

  render() {
    return(
      <HashRouter>
        <Menu />
        <div className="div-body">
          <Switch>
            <Route exact path='/' component={Home} />
            <Route exact path={PathsLabel.Register} component={Register} />
            <Route exact path={PathsLabel.Login} component={Login} />
          </Switch>
        </div>
      </HashRouter>
    );
  }

}

export default App;
