import React,{Component} from 'react';
import {Form, Button} from 'react-bootstrap';

import {PathsLabel} from '../Utils/Paths.js';

class Login extends Component {

    constructor(props) {
        super(props)
        this.state = {
            username: '',
            password: ''
        }
        this.onChange = this.onChange.bind(this); 
        this.login = this.login.bind(this);
    }

    onChange(e) {
        const target = e.target;
        const value = target.value;
        const name = target.name;
        this.setState({ ...this.state, [name]: value });
        console.log(this.state)

    }

    login(e) {
        var url = PathsLabel.ApiProd + PathsLabel.Account + PathsLabel.Login;
        console.log(url)
        let json: User = {
            userId: this.state.username,
            password: this.state.password
        }
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' ,
                        "Access-Control-Allow-Origin": "*"},
            body: JSON.stringify(json)
        };

        fetch(url, requestOptions)
        .then(response => {
            if(response.ok) {
                return response.json();
            }
        })
        .then(json => {
            sessionStorage.setItem('token' , json.tokenId);
            sessionStorage.setItem('id' , json.userId);
            sessionStorage.setItem('role' , json.role);
            window.location.hash = "/";
            window.location.reload();
        })

        e.preventDefault();
        
    }

    render(){
        return(
                <Form>
                <Form.Group className="mb-3" controlId="formUsername">
                    <Form.Label>Username</Form.Label>
                    <Form.Control type="text" name="username" placeholder="Introduza o username" 
                    onChange={this.onChange} value={this.state.username} />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formPassword">
                    <Form.Label>Password</Form.Label>
                    <Form.Control type="password" name="password" placeholder="Introduza a password" 
                    onChange={this.onChange} value={this.state.password} />
                </Form.Group>
                
                <Button variant="primary" type="submit" onClick={this.login}>
                    Confirmar
                </Button>
            </Form>
        );
    }


}

export default Login;