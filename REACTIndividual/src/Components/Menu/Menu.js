import React,{Component} from 'react';
import {Navbar,Nav} from 'react-bootstrap';
import {MenuItem} from './MenuItem';
import {PathsLabel} from '../Utils/Paths.js';

class Menu extends Component {

    constructor(props) {
        super(props);
    }

    setActiveMenuItem(id) {
        sessionStorage.setItem("activeMenuItem", id);
        this.setState({});
    }

    logInMenu() {
        return (
            <Navbar collapseOnSelect bg="light" expand="lg">
                
                    <Navbar.Toggle aria-controls="responsive-navbar-nav" />
                    <Navbar.Collapse id="responsive-navbar-nav">
                    <Nav className="me-auto">
                        <MenuItem id="home" href="/" label="Home" isActive={true}
                            onClick={() => this.setActiveMenuItem("home")} />
                    </Nav>
                    </Navbar.Collapse>
                    <Nav className="ml-auto mr-4 ">
                        <MenuItem id="Logout" href="" label="Terminar Sessão" onClick={this.logout}></MenuItem>
                    </Nav>
                
            </Navbar>
        );
    }

    guestMenu() {
        return (
            <Navbar collapseOnSelect bg="light" expand="lg">
                
                    <Navbar.Toggle aria-controls="responsive-navbar-nav" />
                    <Navbar.Collapse id="responsive-navbar-nav">
                    <Nav className="me-auto">
                        <MenuItem id="home" href="/" label="Home" isActive={true}
                            onClick={() => this.setActiveMenuItem("home")} />
                        <MenuItem id="login" href={PathsLabel.Login} label="Login" isActive={true}
                            onClick={() => this.setActiveMenuItem("login")} />
                            <MenuItem id="register" href={PathsLabel.Register} label="Registo" isActive={true}
                            onClick={() => this.setActiveMenuItem("register")} />
                    </Nav>
                    </Navbar.Collapse>
                
            </Navbar>
            
        );
    }

    render() {
        const loggedIn = sessionStorage.getItem('id') !== null;
        return (
            <div>
                {loggedIn && 
                this.logInMenu()
                }
                {!loggedIn &&
                this.guestMenu()}
            </div>
            
        );
    }

}

export default Menu;