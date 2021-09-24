import React,{Component} from 'react';
import {Form, Button} from 'react-bootstrap';

import {PathsLabel} from '../Utils/Paths.js';

class Register extends Component {

    constructor(props) {
        super(props)
        this.state = {
            username: '',
            name: '',
            url: '',
            emailAccount: '',
            emailRep: '',
            nif: '',
            phone: '',
            address: '',
            description: ''
        }
        this.onChange = this.onChange.bind(this); 
        this.register = this.register.bind(this);
    }

    onChange(e) {
        const target = e.target;
        const value = target.value;
        const name = target.name;
        this.setState({ ...this.state, [name]: value });
        console.log(this.state)

    }

    register(e) {
        var url = PathsLabel.ApiProd + PathsLabel.Account + PathsLabel.Register;
        console.log(url)
        let json: User = {
            userId: this.state.username,
            name: this.state.name,
            userUrl: this.state.url,
            accountEmail: this.state.emailAccount,
            repEmail: this.state.emailRep,
            NIF: this.state.nif,
            phone: this.state.phone,
            address: this.state.address,
            description: this.state.description
        }
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(json)
        };

        fetch(url, requestOptions)
        .then(data => {
            alert('Sucesso')
        })

        e.preventDefault();
    }

    render(){
        return(
                <Form><Form.Group className="mb-3" controlId="formUsername">
                    <Form.Label>Username</Form.Label>
                    <Form.Control type="username" name="username" placeholder="Introduza o username" 
                    onChange={this.onChange} value={this.state.username} />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formName">
                    <Form.Label>Nome da Empresa</Form.Label>
                    <Form.Control type="name" name="name" placeholder="Introduza o nome da empresa" 
                    onChange={this.onChange} value={this.state.name} />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formUrl">
                    <Form.Label>URL</Form.Label>
                    <Form.Control type="text" name="url" placeholder="URL da empresa" 
                    onChange={this.onChange} value={this.state.url} />
                </Form.Group>

                <Form.Group className="mb-3" controlId="fromEmailAccount">
                    <Form.Label>Email da Empresa</Form.Label>
                    <Form.Control type="email" name="emailAccount" placeholder="Introduza o email" 
                    onChange={this.onChange} value={this.state.emailAccount} />
                    <Form.Text className="text-muted">
                    </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3" controlId="formEmailRep">
                    <Form.Label>Email do representante</Form.Label>
                    <Form.Control type="email" name="emailRep" placeholder="Introduza email" 
                    onChange={this.onChange} value={this.state.emailRep} />
                    <Form.Text className="text-muted">
                    </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3" controlId="formNif">
                    <Form.Label>NIF</Form.Label>
                    <Form.Control type="nif" name="nif" placeholder="NIF"
                    onChange={this.onChange} value={this.state.nif}  />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formPhone">
                    <Form.Label>Telefone</Form.Label>
                    <Form.Control type="phone" name="phone" placeholder="Telefone"
                    onChange={this.onChange} value={this.state.phone}  />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formAddress">
                    <Form.Label>Morada</Form.Label>
                    <Form.Control type="address" name="address" placeholder="Morada"
                    onChange={this.onChange} value={this.state.address}  />
                </Form.Group>

                <Form.Group className="mb-3" controlId="formDescription">
                    <Form.Label>Descrição</Form.Label>
                    <Form.Control type="description" name="description" placeholder="Introduza uma pequena descrição" 
                    onChange={this.onChange} value={this.state.description} />
                </Form.Group>
                
                <Button variant="primary" type="submit" onClick={this.register}>
                    Confirmar
                </Button>
            </Form>
        );
    }


}

export default Register;