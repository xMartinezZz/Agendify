import {Endereco} from "./Endereco";
import {Servico} from "./Servico";

export interface Estabelecimento {
  id: string;
  nome: string;
  urlFotoPerfil: string;
  senha:string;
  email: string;
  tipo: number;
  endereco: Endereco;
  cnpj: string;
  descricao: string;
  servicos: Servico[];
}
