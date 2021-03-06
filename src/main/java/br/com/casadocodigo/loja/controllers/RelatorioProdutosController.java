package br.com.casadocodigo.loja.controllers;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import br.com.casadocodigo.loja.dao.ProdutoDAO;
import br.com.casadocodigo.loja.models.Produto;
import br.com.casadocodigo.loja.models.Relatorio;

@Controller
public class RelatorioProdutosController {

  @Autowired
  private ProdutoDAO produtoDAO;

  @RequestMapping(value = "/relatorio-produtos", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public Relatorio getRelatorio(@RequestParam(required = false) Calendar data) {
    List<Produto> produtos = produtoDAO.getProdutosPorData(data);
    
    Relatorio relatorio = new Relatorio(produtos);
       
    return relatorio;
  }

}
