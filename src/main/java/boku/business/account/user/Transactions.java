package boku.business.account.user;

import boku.infra.persistance.BasicRepository;
import com.google.inject.Singleton;

@Singleton
public class Transactions extends BasicRepository<Transaction.TransactionId, Transaction> {

}
