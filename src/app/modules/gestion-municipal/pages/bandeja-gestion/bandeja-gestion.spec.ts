import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BandejaGestion } from './bandeja-gestion';

describe('BandejaGestion', () => {
  let component: BandejaGestion;
  let fixture: ComponentFixture<BandejaGestion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BandejaGestion],
    }).compileComponents();

    fixture = TestBed.createComponent(BandejaGestion);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
